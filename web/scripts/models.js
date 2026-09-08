import { api } from "./api.js";
import { notify } from "./events.js";
import { emptyState, escapeHtml, formatBytes, renderTable, setText, timeAgo } from "./components.js";

export async function mount({ signal, actions }) {
  actions.innerHTML = '<button id="models-refresh" class="button">Actualizar registro</button>';
  const load = async () => {
    const [models, settings] = await Promise.all([api.models(signal), api.settings(signal)]);
    render(models, settings.ai.activeModel);
  };
  document.querySelector("#models-refresh").addEventListener("click", load, { signal });
  document.querySelectorAll("[data-model-action]").forEach(button => button.addEventListener("click", () => notify("Model Manager", `La acción “${button.dataset.modelAction}” requiere selección y confirmación en un endpoint Java; no se modificó ningún archivo.`), { signal }));
  await load();
}

function render(models, activeModel) {
  setText("#active-model", activeModel); setText("#model-count", models.length); setText("#model-storage", formatBytes(models.reduce((total, item) => total + item.bytes, 0)));
  document.querySelector("#model-table").innerHTML = renderTable([
    { label: "Modelo / artefacto", value: row => row.path }, { label: "Tamaño", value: row => formatBytes(row.bytes) },
    { label: "Fecha", value: row => new Date(row.updatedAt).toLocaleString() }, { label: "Estado", value: () => "Disponible", html: () => '<span class="status-badge status-success">AVAILABLE</span>' }
  ], models);
  document.querySelector("#model-checkpoints").innerHTML = models.filter(item => item.path.includes("checkpoint")).map(item => `<div class="checkpoint-row"><div><strong>${escapeHtml(item.path)}</strong><span>${timeAgo(item.updatedAt)}</span></div><small>${formatBytes(item.bytes)}</small></div>`).join("") || emptyState("Sin checkpoints", "El historial aparecerá después de entrenar un modelo.");
}

