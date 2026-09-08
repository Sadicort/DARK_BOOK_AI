import { api } from "./api.js";
import { listen, navigate, notify } from "./events.js";
import { activateProgress, emptyState, escapeHtml, formatNumber, progressRow, setText, stateBadge, timeAgo } from "./components.js";

let visibleEvents = [];
let scannerConfig = {};

export async function mount({ signal, actions }) {
  actions.innerHTML = '<span class="status-badge status-info">Playwright controlled by Java</span>';
  document.querySelectorAll("[data-scanner-action]").forEach(button => button.addEventListener("click", async () => {
    button.disabled = true;
    try { const state = await api.scannerAction(button.dataset.scannerAction, signal); renderState(state); notify("Scanner", `Acción ${button.dataset.scannerAction} completada.`); }
    catch (error) { notify("Error del scanner", error.message, "error"); }
    finally { button.disabled = false; }
  }, { signal }));
  const modeSelect = document.querySelector("#scanner-mode");
  modeSelect?.addEventListener("change", async () => {
    modeSelect.disabled = true;
    try { scannerConfig = await api.saveSettings("scanner", { ...scannerConfig, mode: modeSelect.value }, signal); renderConfig(scannerConfig); notify("Scanner", `Modo cambiado a ${modeSelect.value}.`); }
    catch (error) { notify("No se pudo cambiar el modo", error.message, "error"); modeSelect.value = scannerConfig.mode ?? "demo"; }
    finally { modeSelect.disabled = false; }
  }, { signal });
  document.querySelector("#scanner-open-settings").addEventListener("click", () => navigate("settings?section=scanner"), { signal });
  document.querySelector("#scanner-clear-logs").addEventListener("click", () => { visibleEvents = []; renderLogs(); }, { signal });
  listen("realtime:dashboard", event => { renderState(event.detail.scanner); renderVideo(event.detail.recentVideos[0]); }, signal);
  listen("realtime:events", event => { visibleEvents = event.detail.all.filter(item => item.source === "scanner" || item.type === "VideoCollected"); renderLogs(); }, signal);
  const [state, settings, videos, events] = await Promise.all([api.scanner(signal), api.settings(signal), api.videos(1, signal), api.events(100, signal)]);
  scannerConfig = settings.scanner;
  if (modeSelect) modeSelect.value = scannerConfig.mode ?? "demo";
  renderState(state); renderConfig(scannerConfig); renderVideo(videos[0]);
  visibleEvents = events.filter(item => item.source === "scanner" || item.type === "VideoCollected"); renderLogs();
}

function renderState(state) {
  setText("#scanner-source", state.source);
  const badge = document.querySelector("#scanner-state"); if (badge) badge.outerHTML = stateBadge(state.state).replace(">", ' id="scanner-state">');
  setText("#scanner-seen", formatNumber(state.videosSeen)); setText("#scanner-saved", formatNumber(state.videosSaved)); setText("#scanner-errors", formatNumber(state.errors)); setText("#scanner-time", `${state.activeSeconds}s`);
  const banner = document.querySelector("#scanner-error-banner");
  if (banner) { const message = state.lastError ?? ""; banner.hidden = message === ""; setText("#scanner-error", message); }
}

function renderConfig(config) {
  document.querySelector("#scanner-config").innerHTML = Object.entries(config).filter(([key]) => key !== "seedUrls").map(([key, value]) => `<div class="engine-row"><span>${escapeHtml(key)}</span><strong>${escapeHtml(value)}</strong></div>`).join("");
}

function renderVideo(video) {
  document.querySelector("#scanner-current-video").innerHTML = video ? `<div class="tag-row"><span class="status-badge">${escapeHtml(video.category)}</span><span class="status-badge status-info">${escapeHtml(video.emotion)}</span></div><h3>@${escapeHtml(video.author)}</h3><p>${escapeHtml(video.description)}</p><div class="tag-row">${(video.hashtags ?? []).map(tag => `<span class="tag">${escapeHtml(tag)}</span>`).join("")}</div><small class="muted">Audio: ${escapeHtml(video.audio || "No identificado")}</small>` : emptyState("Sin video actual", "El próximo video guardado aparecerá aquí.");
  const engagement = video ? Math.min(1, Math.log1p(video.likes + 3 * video.comments + 5 * video.shares) / 18) : 0;
  document.querySelector("#scanner-score").innerHTML = progressRow("Engagement estimado", engagement) + progressRow("Completitud de metadatos", video ? 0.82 : 0) + '<div class="engine-row"><span>Modelo</span><span class="status-badge status-warning">Baseline V1</span></div>';
  activateProgress(document.querySelector("#scanner-score"));
}

function renderLogs() {
  const target = document.querySelector("#scanner-log"); if (!target) return;
  target.innerHTML = visibleEvents.slice(0, 40).map(event => `<div class="log-line"><time>${new Date(event.timestamp).toLocaleTimeString()}</time><span>${escapeHtml(event.type)}</span><small>${escapeHtml(event.data?.message ?? event.source)}</small></div>`).join("") || emptyState("Sin logs", "Inicia el scanner para recibir eventos.");
}

