import { api } from "./api.js";
import { escapeHtml } from "./components.js";
import { notify } from "./events.js";

const commands = ["help", "status", "scanner start", "scanner pause", "scanner resume", "scanner stop", "memory ", "models", "datasets", "events", "clear"];
let history = [], historyIndex = 0, eventFilter = "all";

export async function mount({ signal, actions }) {
  actions.innerHTML = '<span class="status-badge status-success">Restricted command set</span>';
  const input = document.querySelector("#terminal-command");
  input.addEventListener("input", () => suggest(input.value), { signal });
  input.addEventListener("keydown", event => onKey(event, input, signal), { signal });
  document.querySelector("#terminal-clear").addEventListener("click", clear, { signal });
  document.querySelector("#terminal-filter").addEventListener("click", () => { eventFilter = eventFilter === "all" ? "errors" : "all"; document.querySelector("#terminal-filter").textContent = `Filtro: ${eventFilter}`; notify("Terminal", `Filtro de eventos: ${eventFilter}`); }, { signal });
  input.focus();
}

async function onKey(event, input, signal) {
  if (event.key === "ArrowUp") { event.preventDefault(); historyIndex = Math.max(0, historyIndex - 1); input.value = history[historyIndex] ?? ""; suggest(input.value); return; }
  if (event.key === "ArrowDown") { event.preventDefault(); historyIndex = Math.min(history.length, historyIndex + 1); input.value = history[historyIndex] ?? ""; suggest(input.value); return; }
  if (event.key === "Tab") { const suggestion = commands.find(command => command.startsWith(input.value)); if (suggestion) { event.preventDefault(); input.value = suggestion; suggest(input.value); } return; }
  if (event.key !== "Enter") return;
  const command = input.value.trim(); if (!command) return;
  history.push(command); historyIndex = history.length; input.value = ""; suggest(""); write(`<span class="terminal-prompt">darkbook&gt;</span> ${escapeHtml(command)}`);
  try { await execute(command, signal); } catch (error) { write(`<span class="terminal-error">${escapeHtml(error.message)}</span>`); }
}

async function execute(command, signal) {
  const [name, ...argumentsList] = command.split(/\s+/);
  if (name === "help") return write("help · status · scanner start|pause|resume|stop · memory &lt;consulta&gt; · models · datasets · events · clear");
  if (name === "clear") return clear();
  if (name === "status") return writeJson(await api.status(signal));
  if (name === "scanner" && ["start", "pause", "resume", "stop"].includes(argumentsList[0])) return writeJson(await api.scannerAction(argumentsList[0], signal));
  if (name === "memory" && argumentsList.length) return writeJson(await api.memorySearch(argumentsList.join(" "), signal));
  if (name === "models") return writeJson(await api.models(signal));
  if (name === "datasets") return writeJson(await api.datasets(signal));
  if (name === "events") { const events = await api.events(80, signal); return writeJson(eventFilter === "errors" ? events.filter(event => event.type === "ErrorDetected") : events); }
  write(`<span class="terminal-error">Comando desconocido. Usa help.</span>`);
}

function suggest(value) { document.querySelector("#terminal-suggestion").textContent = value ? commands.find(command => command.startsWith(value) && command !== value) ?? "" : ""; }
function writeJson(value) { write(escapeHtml(JSON.stringify(value, null, 2)).replace(/\n/g, "<br>").replace(/  /g, "&nbsp;&nbsp;")); }
function write(html) { const target = document.querySelector("#terminal-output"); target.insertAdjacentHTML("beforeend", `<div>${html}</div>`); target.scrollTop = target.scrollHeight; }
function clear() { document.querySelector("#terminal-output").innerHTML = ""; }

