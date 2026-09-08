import { api } from "./api.js";
import { escapeHtml, setText, stateBadge } from "./components.js";
import { notify } from "./events.js";

const sections = [
  ["general", "General", "config"], ["interface", "Interfaz", "ui"], ["ai", "IA", "ai"],
  ["scanner", "Scanner", "scanner"], ["playwright", "Playwright", "playwright"], ["obsidian", "Obsidian", "obsidian"],
  ["sqlite", "SQLite", null], ["python", "Python", "python"], ["performance", "Rendimiento", null],
  ["plugins", "Plugins", null], ["security", "Seguridad", null], ["backups", "Backups", null]
];
let documents = {}, active = "general", baseline = "";

export async function mount({ signal, actions }) {
  actions.innerHTML = '<span class="status-badge status-success">Atomic JSON writes</span>';
  documents = await api.settings(signal);
  const requested = new URLSearchParams(window.location.hash.split("?")[1] ?? "").get("section"); if (sections.some(section => section[0] === requested)) active = requested;
  document.querySelector("#settings-navigation").innerHTML = sections.map(([id, label]) => `<button data-settings-section="${id}">${label}</button>`).join("");
  document.querySelectorAll("[data-settings-section]").forEach(button => button.addEventListener("click", () => { active = button.dataset.settingsSection; render(); }, { signal }));
  document.querySelector("#settings-save").addEventListener("click", () => save(signal), { signal });
  document.querySelector("#settings-reset").addEventListener("click", () => { const source = section()[2]; if (source) document.querySelector("#settings-json").value = baseline; renderFields(source ? JSON.parse(baseline) : virtualDocument(active)); setState("Restaurado", ""); }, { signal });
  document.querySelector("#settings-json").addEventListener("input", () => setState("Cambios pendientes", "status-warning"), { signal });
  render();
}

function render() {
  const [id, label, source] = section();
  document.querySelectorAll("[data-settings-section]").forEach(button => button.setAttribute("aria-selected", String(button.dataset.settingsSection === id)));
  setText("#settings-heading", label);
  const value = source ? documents[source] : virtualDocument(id); baseline = JSON.stringify(value, null, 2);
  const editor = document.querySelector("#settings-json"); editor.value = baseline; editor.disabled = !source;
  document.querySelector("#settings-save").disabled = !source;
  setText("#settings-description", source ? `Editando config/${source}.json mediante Java.` : "Panel informativo; esta categoría aún no tiene un documento persistente dedicado.");
  renderFields(value); setState(source ? "Sin cambios" : "Solo lectura", source ? "" : "status-warning");
}

function renderFields(value) {
  document.querySelector("#settings-form").innerHTML = Object.entries(value).map(([key, item]) => {
    const primitive = typeof item !== "object" || item === null;
    if (typeof item === "boolean") return `<div class="settings-field-row"><div><strong>${escapeHtml(key)}</strong><span>Activar o desactivar esta opción</span></div><button class="switch" role="switch" aria-checked="${item}" data-setting-key="${escapeHtml(key)}"></button></div>`;
    return `<label class="field"><span>${escapeHtml(key)}</span><input class="input" data-setting-key="${escapeHtml(key)}" value="${escapeHtml(primitive ? item : JSON.stringify(item))}" ${primitive ? "" : 'data-json-value="true"'}></label>`;
  }).join("");
  document.querySelectorAll(".switch[data-setting-key]").forEach(button => button.addEventListener("click", () => { button.setAttribute("aria-checked", String(button.getAttribute("aria-checked") !== "true")); syncVisualFields(); }));
  document.querySelectorAll("input[data-setting-key]").forEach(input => input.addEventListener("input", syncVisualFields));
}

function syncVisualFields() {
  const source = section()[2]; if (!source) return;
  try {
    const original = documents[source], next = { ...original };
    document.querySelectorAll("[data-setting-key]").forEach(control => { const key = control.dataset.settingKey; if (control.classList.contains("switch")) next[key] = control.getAttribute("aria-checked") === "true"; else if (control.dataset.jsonValue) next[key] = JSON.parse(control.value); else if (typeof original[key] === "number") next[key] = Number(control.value); else next[key] = control.value; });
    document.querySelector("#settings-json").value = JSON.stringify(next, null, 2); setState("Cambios pendientes", "status-warning");
  } catch { setState("Valor inválido", "status-error"); }
}

async function save(signal) {
  const source = section()[2]; if (!source) return;
  try { const value = JSON.parse(document.querySelector("#settings-json").value); documents[source] = await api.saveSettings(source, value, signal); baseline = JSON.stringify(documents[source], null, 2); renderFields(documents[source]); setState("Guardado", "status-success"); notify("Settings", `${source}.json guardado correctamente.`); }
  catch (error) { setState("Error", "status-error"); notify("Configuración inválida", error.message, "error"); }
}

function section() { return sections.find(item => item[0] === active) ?? sections[0]; }
function setState(label, className) { const target = document.querySelector("#settings-state"); target.className = `status-badge ${className}`; target.textContent = label; }
function virtualDocument(id) { if (id === "sqlite") return { status: "ONLINE", databases: 4, directFrontendAccess: false }; if (id === "performance") return { telemetry: "JVM and OS", gpuAdapter: "Not configured" }; if (id === "plugins") return { manifests: "plugins/*/plugin.json", codeExecution: false }; if (id === "security") return { apiBinding: "127.0.0.1", terminal: "internal commands only" }; return { strategy: "Pending dedicated Java service", automatic: false }; }

