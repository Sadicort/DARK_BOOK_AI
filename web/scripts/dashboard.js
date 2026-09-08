import { api } from "./api.js";
import { listen, openModal } from "./events.js";
import { activateProgress, emptyState, escapeHtml, formatBytes, formatNumber, progressRow, setText, stateBadge, timeAgo } from "./components.js";

export async function mount({ signal, actions }) {
  actions.innerHTML = '<button id="dashboard-refresh" class="button">Sincronizar ahora</button>';
  const refresh = async () => render(await api.dashboard(signal));
  document.querySelector("#dashboard-refresh").addEventListener("click", refresh, { signal });
  document.querySelector("#dashboard-events-all").addEventListener("click", async () => {
    const events = await api.events(100, signal);
    openModal("Historial de eventos", `<div class="timeline">${timeline(events)}</div>`, "EVENT STREAM");
  }, { signal });
  listen("realtime:dashboard", event => render(event.detail), signal);
  await refresh();
}

function render(data) {
  setText("#metric-videos", formatNumber(data.videosTotal));
  setText("#metric-videos-detail", `${formatNumber(data.videosToday)} hoy · ${formatNumber(data.videosLastHour)} última hora`);
  setText("#metric-memory", formatNumber(data.memories));
  setText("#metric-cycles", formatNumber(data.learningCycles));
  setText("#metric-scanner", data.scanner.state);
  setText("#metric-scanner-detail", `${formatNumber(data.scanner.videosSaved)} guardados · ${data.scanner.activeSeconds}s activos`);
  document.querySelector("#dashboard-engines").innerHTML = Object.entries(data.modules).map(([name, state]) => `<div class="engine-row"><span>${escapeHtml(name)}</span>${stateBadge(state)}</div>`).join("");
  const system = data.system;
  const ram = system.totalMemoryBytes ? (system.totalMemoryBytes - system.freeMemoryBytes) / system.totalMemoryBytes : system.heapUsedBytes / system.heapMaxBytes;
  const disk = system.diskTotalBytes ? (system.diskTotalBytes - system.diskFreeBytes) / system.diskTotalBytes : 0;
  const resources = [["CPU", system.cpuLoad ?? 0], ["RAM", ram], ["Disco", disk]];
  document.querySelector("#dashboard-resources").innerHTML = resources.map(([label, value]) => progressRow(label, value)).join("") + `<div class="engine-row"><span>GPU / VRAM</span><span class="status-badge status-warning">Adaptador requerido</span></div><div class="engine-row"><span>Heap Java</span><strong>${formatBytes(system.heapUsedBytes)} / ${formatBytes(system.heapMaxBytes)}</strong></div>`;
  activateProgress(document.querySelector("#dashboard-resources"));
  document.querySelector("#dashboard-timeline").innerHTML = timeline(data.events.slice(0, 8)) || emptyState("Sin actividad", "Los eventos del sistema aparecerán aquí.");
  renderTrends(data.recentVideos);
  renderCurrentVideo(data.recentVideos[0]);
  const scannerBadge = document.querySelector("#dashboard-scanner-badge");
  scannerBadge.outerHTML = stateBadge(data.scanner.state).replace("status-badge", "status-badge").replace(">", ' id="dashboard-scanner-badge">');
}

function renderTrends(videos) {
  const totals = new Map();
  videos.forEach(video => totals.set(video.category, (totals.get(video.category) ?? 0) + 1));
  const sorted = [...totals.entries()].sort((left, right) => right[1] - left[1]);
  const maximum = sorted[0]?.[1] ?? 1;
  document.querySelector("#dashboard-trends").innerHTML = sorted.map(([name, count]) => `<div class="trend-row"><div><strong>${escapeHtml(name)}</strong><span>${count} videos recientes</span><div class="progress-track"><i data-progress="${Math.round(count / maximum * 100)}"></i></div></div><b>${Math.round(count / Math.max(1, videos.length) * 100)}%</b></div>`).join("") || emptyState("Sin tendencias", "Analiza videos para detectar categorías frecuentes.");
  activateProgress(document.querySelector("#dashboard-trends"));
}

function renderCurrentVideo(video) {
  document.querySelector("#dashboard-current-video").innerHTML = video ? `<div class="tag-row"><span class="status-badge status-info">${escapeHtml(video.platform)}</span><span class="status-badge">${escapeHtml(video.category)}</span><span class="status-badge">${escapeHtml(video.emotion)}</span></div><h3>@${escapeHtml(video.author)}</h3><p>${escapeHtml(video.description)}</p><div class="tag-row">${(video.hashtags ?? []).map(tag => `<span class="tag">${escapeHtml(tag)}</span>`).join("")}</div><small class="muted">♥ ${formatNumber(video.likes)} · ◉ ${formatNumber(video.comments)} · ↗ ${formatNumber(video.shares)}</small>` : emptyState("Scanner inactivo", "Inicia el scanner para alimentar el sistema.");
}

function timeline(events) {
  return events.map(event => `<div class="timeline-item"><i></i><div><strong>${escapeHtml(event.type)}</strong><span>${escapeHtml(event.source)}${event.data?.message ? ` · ${escapeHtml(event.data.message)}` : ""}</span></div><time>${timeAgo(event.timestamp)}</time></div>`).join("");
}

