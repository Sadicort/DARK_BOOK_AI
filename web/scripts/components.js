export function escapeHtml(value) {
  return String(value ?? "").replace(/[&<>'"]/g, character => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", "'": "&#39;", '"': "&quot;" })[character]);
}

export function formatNumber(value) { return new Intl.NumberFormat("es").format(value ?? 0); }

export function formatBytes(value = 0) {
  if (!value) return "0 B";
  const units = ["B", "KB", "MB", "GB", "TB"];
  const index = Math.min(units.length - 1, Math.floor(Math.log(value) / Math.log(1024)));
  return `${(value / (1024 ** index)).toFixed(index ? 1 : 0)} ${units[index]}`;
}

export function timeAgo(value) {
  const seconds = Math.max(0, Math.floor((Date.now() - new Date(value).getTime()) / 1000));
  if (seconds < 60) return `${seconds}s`;
  if (seconds < 3600) return `${Math.floor(seconds / 60)}m`;
  if (seconds < 86400) return `${Math.floor(seconds / 3600)}h`;
  return `${Math.floor(seconds / 86400)}d`;
}

export function stateBadge(value) {
  const state = String(value ?? "UNKNOWN").toUpperCase();
  const className = state.includes("ONLINE") || state.includes("RUNNING") || state.includes("COMPLETED") ? "status-success" : state.includes("ERROR") || state.includes("FAILED") || state.includes("UNAVAILABLE") ? "status-error" : state.includes("READY") || state.includes("PAUSED") ? "status-warning" : "";
  return `<span class="status-badge ${className}">${escapeHtml(state)}</span>`;
}

export function emptyState(title, message) {
  return `<div class="empty-state"><strong>${escapeHtml(title)}</strong><span>${escapeHtml(message)}</span></div>`;
}

export function errorState(message) {
  return `<div class="error-state"><strong>No se pudieron cargar los datos</strong><span>${escapeHtml(message)}</span></div>`;
}

export function progressRow(label, value) {
  const percent = Math.round(Math.max(0, Math.min(1, Number(value) || 0)) * 100);
  return `<div class="progress"><div class="progress-label"><span>${escapeHtml(label)}</span><strong>${percent}%</strong></div><div class="progress-track"><i data-progress="${percent}"></i></div></div>`;
}

export function activateProgress(container) {
  requestAnimationFrame(() => container?.querySelectorAll("[data-progress]").forEach(bar => { bar.style.width = `${bar.dataset.progress}%`; }));
}

export function renderTable(columns, rows) {
  if (!rows.length) return emptyState("Sin registros", "No hay elementos disponibles para esta vista.");
  return `<div class="table-shell"><table><thead><tr>${columns.map(column => `<th>${escapeHtml(column.label)}</th>`).join("")}</tr></thead><tbody>${rows.map(row => `<tr>${columns.map(column => `<td title="${escapeHtml(column.value(row))}">${column.html ? column.html(row) : escapeHtml(column.value(row))}</td>`).join("")}</tr>`).join("")}</tbody></table></div>`;
}

export function setText(selector, value) { const element = document.querySelector(selector); if (element) element.textContent = value; }

