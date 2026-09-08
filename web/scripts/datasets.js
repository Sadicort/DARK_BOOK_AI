import { api } from "./api.js";
import { notify } from "./events.js";
import { emptyState, escapeHtml, formatBytes, renderTable, timeAgo } from "./components.js";

let datasets = [];

export async function mount({ signal, actions }) {
  actions.innerHTML = '<span id="dataset-count" class="status-badge status-info">0 archivos</span>';
  document.querySelector("#dataset-search").addEventListener("input", render, { signal });
  document.querySelector("#dataset-filter").addEventListener("change", render, { signal });
  document.querySelector("#dataset-search-clear").addEventListener("click", () => { document.querySelector("#dataset-search").value = ""; render(); }, { signal });
  document.querySelectorAll("[data-dataset-action]").forEach(button => button.addEventListener("click", () => notify("Dataset Manager", `La operación “${button.dataset.datasetAction}” necesita un endpoint Java dedicado y no se ejecutó.`), { signal }));
  try { datasets = await api.datasets(signal); render(); renderHistory(); }
  catch (error) { document.querySelector("#dataset-table").innerHTML = emptyState("No se pudieron cargar datasets", error.message); }
}

function render() {
  const query = document.querySelector("#dataset-search").value.toLowerCase(), filter = document.querySelector("#dataset-filter").value;
  const rows = datasets.filter(item => item.path.toLowerCase().includes(query) && (filter === "all" || item.path.includes(`/${filter}/`)));
  document.querySelector("#dataset-count").textContent = `${rows.length} archivos`;
  document.querySelector("#dataset-table").innerHTML = renderTable([
    { label: "Archivo", value: row => row.path }, { label: "Tipo", value: row => row.path.split("/")[1] ?? "root" },
    { label: "Tamaño", value: row => formatBytes(row.bytes) }, { label: "Actualizado", value: row => new Date(row.updatedAt).toLocaleString() },
    { label: "Estado", value: () => "Ready", html: () => '<span class="status-badge status-success">READY</span>' }
  ], rows);
  document.querySelectorAll("#dataset-table tbody tr").forEach((row, index) => row.addEventListener("click", () => preview(rows[index])));
}

function preview(item) {
  document.querySelector("#dataset-preview-name").textContent = item.path;
  document.querySelector("#dataset-preview").textContent = JSON.stringify({ path: item.path, bytes: item.bytes, updatedAt: item.updatedAt, access: "El contenido requiere un endpoint de vista previa controlado por Java." }, null, 2);
}

function renderHistory() {
  document.querySelector("#dataset-history").innerHTML = datasets.slice().sort((a, b) => new Date(b.updatedAt) - new Date(a.updatedAt)).slice(0, 8).map(item => `<div class="timeline-item"><i></i><div><strong>${escapeHtml(item.path)}</strong><span>${formatBytes(item.bytes)}</span></div><time>${timeAgo(item.updatedAt)}</time></div>`).join("") || emptyState("Sin historial", "Los datasets aparecerán cuando el scanner guarde ejemplos.");
}

