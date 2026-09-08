import { api } from "./api.js";
import { listen, notify } from "./events.js";
import { emptyState, escapeHtml, formatNumber, setText, timeAgo } from "./components.js";

export async function mount({ signal, actions }) {
  actions.innerHTML = '<span class="status-badge status-info">Cosine retrieval</span>';
  const search = () => runSearch(signal);
  document.querySelector("#memory-search").addEventListener("click", search, { signal });
  document.querySelector("#memory-query").addEventListener("keydown", event => { if (event.key === "Enter") search(); }, { signal });
  document.querySelector("#memory-clear").addEventListener("click", () => { document.querySelector("#memory-query").value = ""; document.querySelector("#memory-results").innerHTML = emptyState("Esperando consulta", "Escribe un concepto para recuperar recuerdos."); }, { signal });
  document.querySelector("#reasoning-run").addEventListener("click", () => runReasoning(signal), { signal });
  listen("realtime:dashboard", event => renderOverview(event.detail), signal);
  const dashboard = await api.dashboard(signal); renderOverview(dashboard);
  document.querySelector("#memory-results").innerHTML = emptyState("Esperando consulta", "Escribe un concepto para recuperar recuerdos.");
}

function renderOverview(data) {
  setText("#episodic-count", formatNumber(data.memories)); setText("#semantic-count", formatNumber(data.memories));
  document.querySelector("#memory-timeline").innerHTML = data.events.filter(event => event.type === "MemoryStored" || event.type === "VideoCollected").slice(0, 8).map(event => `<div class="timeline-item"><i></i><div><strong>${escapeHtml(event.type)}</strong><span>${escapeHtml(event.data?.sourceId ?? event.source)}</span></div><time>${timeAgo(event.timestamp)}</time></div>`).join("") || emptyState("Timeline vacío", "Los recuerdos nuevos aparecerán aquí.");
}

async function runSearch(signal) {
  const query = document.querySelector("#memory-query").value.trim(); if (!query) return notify("Memory Search", "Escribe una consulta.");
  const target = document.querySelector("#memory-results"); target.innerHTML = '<div class="loading-state"><div class="loader-ring"></div><span>Recuperando memoria…</span></div>';
  try { const rows = await api.memorySearch(query, signal); setText("#memory-result-caption", `${rows.length} recuerdos relacionados`); target.innerHTML = rows.map(row => `<div class="memory-result"><div><small>${escapeHtml(row.kind)} · ${new Date(row.createdAt).toLocaleString()}</small><p>${escapeHtml(row.content)}</p></div><strong class="memory-score">${(row.similarity * 100).toFixed(1)}%</strong></div>`).join("") || emptyState("Sin coincidencias", "No existe evidencia relacionada en la memoria actual."); }
  catch (error) { target.innerHTML = emptyState("Búsqueda fallida", error.message); }
}

async function runReasoning(signal) {
  const question = document.querySelector("#reasoning-question").value.trim(); if (!question) return notify("Reasoning Inspector", "Escribe una pregunta.");
  const target = document.querySelector("#reasoning-result"); target.innerHTML = '<div class="loading-state"><div class="loader-ring"></div><span>Relacionando evidencia…</span></div>';
  try { const result = await api.reason(question, signal); target.innerHTML = `<div class="card"><span class="eyebrow">RESPUESTA · ${(result.confidence * 100).toFixed(1)}% CONFIANZA</span><p>${escapeHtml(result.answer)}</p><div class="resource-list">${result.evidence.map(item => `<div class="engine-row"><span>${escapeHtml(item.content)}</span><strong>${(item.similarity * 100).toFixed(0)}%</strong></div>`).join("")}</div></div>`; }
  catch (error) { target.innerHTML = emptyState("Razonamiento fallido", error.message); }
}

