import { api } from "./api.js";
import { listen, notify } from "./events.js";
import { emptyState, escapeHtml, formatBytes, formatNumber, setText, stateBadge, timeAgo } from "./components.js";

export async function mount({ signal, actions }) {
  actions.innerHTML = '<span class="status-badge status-success">Neural architecture online</span>';
  document.querySelector("#training-start").addEventListener("click", () => trainingAction("start", signal), { signal });
  document.querySelector("#training-cancel").addEventListener("click", () => trainingAction("cancel", signal), { signal });
  document.querySelector("#training-pause").addEventListener("click", () => notify("Trainer", "La API actual todavía no expone pausa segura; el proceso continúa sin interrupción."), { signal });
  document.querySelector("#training-save").addEventListener("click", () => notify("Checkpoint", "Los checkpoints se guardan al completar el proceso de entrenamiento."), { signal });
  document.querySelector("#checkpoint-refresh").addEventListener("click", () => loadModels(signal), { signal });
  document.querySelector("#inference-run").addEventListener("click", () => runInference(signal), { signal });
  listen("realtime:dashboard", event => { renderTraining(event.detail.training); setText("#embedding-count", formatNumber(event.detail.memories)); renderVocabulary(event.detail.recentVideos); }, signal);
  const [dashboard, settings] = await Promise.all([api.dashboard(signal), api.settings(signal)]);
  renderTraining(dashboard.training); setText("#embedding-dimension", `${settings.ai.embeddingDimension}D`); setText("#embedding-count", formatNumber(dashboard.memories)); renderVocabulary(dashboard.recentVideos); renderTrainingLogs(dashboard.events); await loadModels(signal);
}

async function trainingAction(action, signal) {
  try { const state = action === "start" ? await api.startTraining(signal) : await api.cancelTraining(signal); renderTraining(state); notify("Neural Trainer", action === "start" ? "Entrenamiento iniciado." : "Entrenamiento cancelado."); }
  catch (error) { notify("Trainer no disponible", error.message, "error"); }
}

function renderTraining(training) {
  const badge = document.querySelector("#training-status"); if (badge) badge.outerHTML = stateBadge(training.state).replace(">", ' id="training-status">');
  setText("#training-message", training.message);
  const progress = training.state === "RUNNING" ? 38 : training.state === "COMPLETED" ? 100 : 0;
  const bar = document.querySelector("#training-progress"); if (bar) bar.style.width = `${progress}%`; setText("#training-progress-label", `${progress}%`);
}

function renderVocabulary(videos) {
  const counts = new Map();
  videos.flatMap(video => String(video.description).toLowerCase().match(/[\p{L}\p{N}#@]{3,}/gu) ?? []).forEach(word => counts.set(word, (counts.get(word) ?? 0) + 1));
  const words = [...counts.entries()].sort((left, right) => right[1] - left[1]).slice(0, 24);
  setText("#vocabulary-count", `${words.length} términos`);
  document.querySelector("#vocabulary-cloud").innerHTML = words.map(([word, count]) => `<span>${escapeHtml(word)} · ${count}</span>`).join("") || emptyState("Sin vocabulario", "Analiza videos para construir el vocabulario.");
}

async function loadModels(signal) {
  const models = await api.models(signal);
  document.querySelector("#checkpoint-list").innerHTML = models.slice(0, 7).map(model => `<div class="checkpoint-row"><div><strong>${escapeHtml(model.path)}</strong><span>${new Date(model.updatedAt).toLocaleString()}</span></div><small>${formatBytes(model.bytes)}</small></div>`).join("") || emptyState("Sin checkpoints", "Entrena un modelo para crear el primer artefacto.");
}

function renderTrainingLogs(events) {
  const rows = events.filter(event => event.source === "neural" || event.type.includes("Training"));
  document.querySelector("#training-console").innerHTML = rows.map(event => `<div>[${new Date(event.timestamp).toLocaleTimeString()}] ${escapeHtml(event.type)} — ${escapeHtml(event.data?.message ?? event.source)}</div>`).join("") || '<div>[READY] Neural Core esperando entrenamiento.</div>';
}

async function runInference(signal) {
  const text = document.querySelector("#inference-text").value.trim(); if (!text) return notify("Inference Lab", "Escribe un texto para clasificar.");
  try { const result = await api.infer(text, signal); document.querySelector("#inference-result").innerHTML = `<div class="engine-row"><span>${escapeHtml(result.label)}</span>${stateBadge(result.modelLoaded ? "ONLINE" : "UNTRAINED")}</div><p>${escapeHtml(result.explanation)} · Confianza ${(result.confidence * 100).toFixed(1)}%</p>`; }
  catch (error) { notify("Inferencia fallida", error.message, "error"); }
}

