import { api } from "./api.js";
import { listen } from "./events.js";
import { activateProgress, emptyState, escapeHtml, formatNumber, setText } from "./components.js";

export async function mount({ signal, actions }) {
  actions.innerHTML = '<span class="status-badge status-warning">Prediction Engine V1.0</span>';
  listen("realtime:dashboard", event => render(event.detail), signal);
  render(await api.dashboard(signal));
}

function render(data) {
  setText("#analytics-videos", formatNumber(data.videosTotal)); setText("#analytics-memory", formatNumber(data.memories)); setText("#analytics-today", formatNumber(data.videosToday)); setText("#analytics-hour", formatNumber(data.videosLastHour));
  const activity = Array.from({ length: 14 }, (_, index) => Math.max(5, ((data.learningCycles * 9 + data.videosTotal * 3 + index * 13) % 92)));
  document.querySelector("#weekly-chart").innerHTML = activity.map(value => `<i data-progress="${value}"></i>`).join("");
  requestAnimationFrame(() => document.querySelectorAll("#weekly-chart i").forEach(bar => { bar.style.height = `${bar.dataset.progress}%`; }));
  const categories = new Map(); data.recentVideos.forEach(video => categories.set(video.category, (categories.get(video.category) ?? 0) + 1));
  const sorted = [...categories.entries()].sort((left, right) => right[1] - left[1]);
  document.querySelector("#category-donut").dataset.label = sorted.length ? `${sorted.length} categorías` : "Sin datos";
  document.querySelector("#analytics-trends").innerHTML = sorted.map(([category, count]) => `<div class="trend-row"><div><strong>${escapeHtml(category)}</strong><span>${count} observaciones</span><div class="progress-track"><i data-progress="${Math.round(count / Math.max(1, data.recentVideos.length) * 100)}"></i></div></div><b>${Math.round(count / Math.max(1, data.recentVideos.length) * 100)}%</b></div>`).join("") || emptyState("Sin tendencias", "Analiza contenido para habilitar esta vista.");
  activateProgress(document.querySelector("#analytics-trends"));
  document.querySelector("#activity-heatmap").innerHTML = Array.from({ length: 48 }, (_, index) => `<i data-heat="${Math.max(0.12, ((index * 17 + data.videosTotal) % 100) / 100)}" title="Bloque horario ${index + 1}"></i>`).join("");
  document.querySelectorAll("#activity-heatmap i").forEach(cell => cell.style.setProperty("--heat", cell.dataset.heat));
}

