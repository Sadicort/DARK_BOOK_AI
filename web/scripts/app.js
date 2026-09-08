import { api } from "./api.js";
import { appEvents, emit, notify, navigate } from "./events.js";
import { realtime } from "./websocket.js";
import { startRouter } from "./router.js";

const permanentController = new AbortController();
const { signal } = permanentController;

async function fragment(path) {
  const response = await fetch(path);
  if (!response.ok) throw new Error(`No se pudo cargar ${path}`);
  return response.text();
}

async function composeInterface() {
  const [layout, sidebar, navbar, footer, modal, toast] = await Promise.all([
    fragment("/layouts/app-layout.html"), fragment("/components/sidebar.html"),
    fragment("/components/navbar.html"), fragment("/components/footer.html"),
    fragment("/components/modal.html"), fragment("/components/toast.html")
  ]);
  document.querySelector("#app-root").innerHTML = layout;
  document.querySelector("#sidebar-slot").innerHTML = sidebar;
  document.querySelector("#navbar-slot").innerHTML = navbar;
  document.querySelector("#footer-slot").innerHTML = footer;
  document.querySelector("#overlay-root").innerHTML = modal + toast;
  restorePreferences();
  bindShell();
}

function bindShell() {
  document.querySelector("#sidebar-collapse").addEventListener("click", toggleSidebar, { signal });
  document.querySelector("#mobile-navigation").addEventListener("click", () => document.querySelector("#os-shell").classList.toggle("mobile-menu-open"), { signal });
  document.querySelectorAll("[data-route]").forEach(link => link.addEventListener("click", () => document.querySelector("#os-shell").classList.remove("mobile-menu-open"), { signal }));
  document.querySelector("#quick-settings").addEventListener("click", () => navigate("settings"), { signal });
  document.querySelector("#navbar-settings").addEventListener("click", () => navigate("settings"), { signal });
  document.querySelector("#theme-button").addEventListener("click", toggleTheme, { signal });
  document.querySelector("#language-button").addEventListener("click", showLanguages, { signal });
  document.querySelector("#notification-button").addEventListener("click", showNotifications, { signal });
  document.querySelector("#global-search-button").addEventListener("click", showGlobalSearch, { signal });
  document.querySelector("#modal-close").addEventListener("click", closeModal, { signal });
  document.addEventListener("keydown", event => {
    if ((event.ctrlKey || event.metaKey) && event.key.toLowerCase() === "k") { event.preventDefault(); showGlobalSearch(); }
    if (event.key === "Escape") closeModal();
  }, { signal });
  window.addEventListener("darkbook:latency", event => { document.querySelector("#footer-api").textContent = `API ${event.detail} ms`; }, { signal });
  appEvents.addEventListener("toast", event => renderToast(event.detail), { signal });
  appEvents.addEventListener("modal", event => showModal(event.detail), { signal });
  appEvents.addEventListener("route:changed", () => document.querySelector("#os-shell").classList.remove("mobile-menu-open"), { signal });
  appEvents.addEventListener("realtime:dashboard", event => renderGlobalDashboard(event.detail), { signal });
  appEvents.addEventListener("realtime:status", event => renderGlobalStatus(event.detail), { signal });
  appEvents.addEventListener("realtime:events", event => renderGlobalEvents(event.detail), { signal });
  appEvents.addEventListener("realtime:error", event => { document.querySelector("#footer-system-state").textContent = "API OFFLINE"; document.querySelector("#footer-system-state").classList.add("status-error"); document.querySelector("#footer-message").textContent = event.detail.message; }, { signal });
  window.setInterval(updateClock, 1000);
  updateClock();
}

async function boot() {
  const stages = ["Configuration Manager", "SQLite Storage", "Obsidian Vault", "Memory Index", "Neural Core", "Dashboard API"];
  const log = document.querySelector("#boot-log");
  for (let index = 0; index < stages.length; index += 1) {
    log.insertAdjacentHTML("beforeend", `<li><span>Loading ${stages[index]}…</span><b>OK</b></li>`);
    document.querySelector("#boot-progress > i").style.width = `${((index + 1) / stages.length) * 100}%`;
    await delay(120);
  }
  try {
    const status = await api.status();
    renderGlobalStatus(status);
  } catch (error) {
    log.insertAdjacentHTML("beforeend", `<li><span>${escapeHtml(error.message)}</span><b>ERROR</b></li>`);
  }
  await delay(240);
  document.querySelector("#boot-screen").classList.add("is-complete");
}

function renderGlobalDashboard(data) {
  setText("#panel-memory", formatNumber(data.memories));
  setText("#panel-videos", formatNumber(data.videosTotal));
  setText("#panel-cycles", formatNumber(data.learningCycles));
  setText("#footer-java", `JAVA ${data.system.javaVersion ?? "—"}`);
  setText("#footer-message", `${data.scanner.state} · ${formatNumber(data.videosTotal)} videos · ${formatNumber(data.memories)} memorias`);
  const heap = ratio(data.system.heapUsedBytes, data.system.heapMaxBytes);
  const memory = data.system.totalMemoryBytes ? ratio(data.system.totalMemoryBytes - data.system.freeMemoryBytes, data.system.totalMemoryBytes) : heap;
  const disk = data.system.diskTotalBytes ? ratio(data.system.diskTotalBytes - data.system.diskFreeBytes, data.system.diskTotalBytes) : 0;
  setProgress("panel-cpu", data.system.cpuLoad ?? 0);
  setProgress("panel-ram", memory);
  setProgress("panel-disk", disk);
  const sidebarResources = document.querySelector("#sidebar-resource-list");
  if (sidebarResources) sidebarResources.innerHTML = `<div class="service-tile"><span>CPU</span><strong>${Math.round((data.system.cpuLoad ?? 0) * 100)}%</strong></div><div class="service-tile"><span>RAM</span><strong>${Math.round(memory * 100)}%</strong></div><div class="service-tile"><span>GPU</span><strong>—</strong></div>`;
}

function renderGlobalStatus(status) {
  const navbarStates = [{ key: "AI", value: status.neural }, { key: "SCANNER", value: status.scanner }, { key: "MEMORY", value: status.memory }, { key: "MODEL", value: status.neural }];
  const serviceStates = ["sqlite", "obsidian", "playwright", "python"].map(key => ({ key, value: status[key] ?? (key === "playwright" ? "READY" : "UNKNOWN") }));
  document.querySelector("#navbar-states").innerHTML = navbarStates.map(item => `<span class="navbar-state"><i class="status-dot ${statusClass(item.value)}"></i>${escapeHtml(item.key)}</span>`).join("");
  document.querySelector("#sidebar-service-list").innerHTML = serviceStates.map(item => `<div class="service-tile"><i class="status-dot ${statusClass(item.value)}"></i><span>${escapeHtml(item.key)}</span></div>`).join("");
  setText("#panel-ai-state", status.neural?.includes("ONLINE") ? "ONLINE" : status.neural ?? "UNKNOWN");
  setText("#sidebar-core-state", status.neural?.includes("ONLINE") ? "ONLINE" : status.neural ?? "UNKNOWN");
}

function renderGlobalEvents(payload) {
  document.querySelector("#notification-count").textContent = Math.min(payload.all.length, 99);
  document.querySelector("#panel-events").innerHTML = payload.all.slice(0, 5).map(event => `<div class="panel-event"><i></i><span>${escapeHtml(event.type)}</span></div>`).join("") || '<div class="muted">Sin actividad reciente.</div>';
  payload.fresh.filter(event => event.type === "ErrorDetected").forEach(event => notify("Error del sistema", event.data?.message ?? event.type, "error"));
}

async function showGlobalSearch() {
  showModal({ title: "Búsqueda global", kicker: "COMMAND SEARCH", content: '<div class="search-field"><span>⌕</span><input id="global-search-input" type="search" placeholder="Videos, hashtags, personas, conceptos, modelos o datasets…"><button id="global-search-clear">×</button></div><div id="global-search-results" class="resource-list section-gap"><div class="empty-state"><strong>Explora todo Dark Book</strong><span>Escribe al menos dos caracteres.</span></div></div>' });
  const input = document.querySelector("#global-search-input");
  input.focus();
  let sequence = 0;
  input.addEventListener("input", async () => {
    const query = input.value.trim(); const current = ++sequence;
    if (query.length < 2) return;
    const target = document.querySelector("#global-search-results");
    target.innerHTML = '<div class="loading-state"><div class="loader-ring"></div><span>Buscando…</span></div>';
    const results = await Promise.allSettled([api.videos(100), api.memorySearch(query), api.models(), api.datasets()]);
    if (current !== sequence) return;
    const normalized = query.toLowerCase();
    const videos = results[0].status === "fulfilled" ? results[0].value.filter(video => [video.description, video.author, ...(video.hashtags ?? [])].some(value => String(value).toLowerCase().includes(normalized))) : [];
    const memories = results[1].status === "fulfilled" ? results[1].value : [];
    const models = results[2].status === "fulfilled" ? results[2].value.filter(item => item.path.toLowerCase().includes(normalized)) : [];
    const datasets = results[3].status === "fulfilled" ? results[3].value.filter(item => item.path.toLowerCase().includes(normalized)) : [];
    const rows = [...videos.slice(0, 5).map(item => ({ type: "VIDEO", text: `${item.author} · ${item.description}` })), ...memories.slice(0, 5).map(item => ({ type: "MEMORY", text: item.content })), ...models.slice(0, 3).map(item => ({ type: "MODEL", text: item.path })), ...datasets.slice(0, 3).map(item => ({ type: "DATASET", text: item.path }))];
    target.innerHTML = rows.map(row => `<div class="engine-row"><span>${escapeHtml(row.text)}</span><span class="status-badge status-info">${row.type}</span></div>`).join("") || '<div class="empty-state"><strong>Sin resultados</strong><span>No existe una coincidencia en la memoria local.</span></div>';
  });
  document.querySelector("#global-search-clear").addEventListener("click", () => { input.value = ""; input.dispatchEvent(new Event("input")); input.focus(); });
}

function showNotifications() {
  const events = window.__darkbookEvents ?? [];
  const content = events.map(event => `<div class="timeline-item"><i></i><div><strong>${escapeHtml(event.type)}</strong><span>${escapeHtml(event.source)} · ${escapeHtml(event.data?.message ?? "Evento registrado")}</span></div><time>${timeAgo(event.timestamp)}</time></div>`).join("") || '<div class="empty-state"><strong>Sin notificaciones</strong><span>Los nuevos eventos aparecerán aquí.</span></div>';
  showModal({ title: "Notification Center", kicker: "EVENT STREAM", content });
}

function showLanguages() {
  showModal({ title: "Idioma", kicker: "LOCALIZATION", content: '<div class="engine-row"><span><strong>Español</strong><br><small class="muted">Idioma activo de Dark Book OS</small></span><span class="status-badge status-success">Activo</span></div><div class="engine-row section-gap"><span><strong>English</strong><br><small class="muted">Paquete de traducción en preparación</small></span><span class="status-badge">Próximamente</span></div>' });
}

function showModal({ title, content, kicker = "DARK BOOK OS" }) {
  const modal = document.querySelector("#global-modal");
  setText("#modal-title", title); setText("#modal-kicker", kicker);
  document.querySelector("#modal-content").innerHTML = content;
  if (!modal.open) modal.showModal();
}

function closeModal() { const modal = document.querySelector("#global-modal"); if (modal?.open) modal.close(); }

function renderToast({ title, message, variant }) {
  const toast = document.createElement("div");
  toast.className = `toast ${variant === "error" ? "toast-error" : ""}`;
  toast.innerHTML = `<strong>${escapeHtml(title)}</strong><span>${escapeHtml(message)}</span>`;
  document.querySelector("#toast-region").append(toast);
  window.setTimeout(() => toast.remove(), 4800);
}

function toggleSidebar() {
  const shell = document.querySelector("#os-shell"); shell.classList.toggle("sidebar-collapsed");
  localStorage.setItem("darkbook.sidebar", shell.classList.contains("sidebar-collapsed") ? "collapsed" : "expanded");
}

function toggleTheme() {
  const next = document.documentElement.dataset.theme === "midnight" ? "purple" : "midnight";
  document.documentElement.dataset.theme = next === "purple" ? "" : next;
  localStorage.setItem("darkbook.theme", next);
  notify("Tema actualizado", next === "purple" ? "Dark Book Purple" : "Blue Core Midnight");
}

function restorePreferences() {
  if (localStorage.getItem("darkbook.sidebar") === "collapsed") document.querySelector("#os-shell").classList.add("sidebar-collapsed");
  if (localStorage.getItem("darkbook.theme") === "midnight") document.documentElement.dataset.theme = "midnight";
}

function updateClock() {
  const now = new Date();
  setText("#navbar-time", now.toLocaleTimeString("es", { hour12: false }));
  setText("#navbar-date", now.toLocaleDateString("es", { weekday: "long", day: "2-digit", month: "short", year: "numeric" }));
  setText("#panel-clock", now.toLocaleTimeString("es", { hour: "2-digit", minute: "2-digit", hour12: false }));
}

function setProgress(id, value) { const percent = Math.round(Math.max(0, Math.min(1, value)) * 100); const element = document.getElementById(id); if (element) element.style.width = `${percent}%`; setText(`#${id}-label`, `${percent}%`); }
function ratio(value, total) { return total ? value / total : 0; }
function setText(selector, value) { const element = document.querySelector(selector); if (element) element.textContent = value; }
function formatNumber(value) { return new Intl.NumberFormat("es").format(value ?? 0); }
function timeAgo(value) { const seconds = Math.max(0, Math.floor((Date.now() - new Date(value).getTime()) / 1000)); return seconds < 60 ? `${seconds}s` : seconds < 3600 ? `${Math.floor(seconds / 60)}m` : `${Math.floor(seconds / 3600)}h`; }
function statusClass(value) { const text = String(value).toUpperCase(); return text.includes("ONLINE") || text.includes("RUNNING") || text.includes("COMPLETED") ? "status-online" : text.includes("ERROR") || text.includes("FAILED") || text.includes("UNAVAILABLE") ? "status-error" : ""; }
function escapeHtml(value) { return String(value ?? "").replace(/[&<>'"]/g, character => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", "'": "&#39;", '"': "&quot;" })[character]); }
function delay(milliseconds) { return new Promise(resolve => window.setTimeout(resolve, milliseconds)); }

appEvents.addEventListener("realtime:events", event => { window.__darkbookEvents = event.detail.all; }, { signal });

try {
  await composeInterface();
  await Promise.all([boot(), startRouter()]);
  realtime.start();
} catch (error) {
  document.querySelector("#app-root").innerHTML = `<div class="error-state"><strong>No se pudo iniciar Dark Book OS</strong><span>${escapeHtml(error.message)}</span></div>`;
  document.querySelector("#boot-screen")?.classList.add("is-complete");
}
