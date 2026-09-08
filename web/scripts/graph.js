import { api } from "./api.js";
import { emptyState, escapeHtml, stateBadge } from "./components.js";
import { listen, notify } from "./events.js";

let graphController = null;

export async function mount({ signal, actions }) {
  actions.innerHTML = '<span id="graph-count" class="status-badge status-info">0 nodes</span>';
  const load = async () => {
    try { const graph = await api.graph(signal); graphController?.destroy(); graphController = createGraph(document.querySelector("#knowledge-canvas"), graph, signal); renderSideData(graph, signal); }
    catch (error) { document.querySelector(".graph-stage").innerHTML = emptyState("No se pudo cargar el grafo", error.message); }
  };
  document.querySelector("#graph-refresh").addEventListener("click", load, { signal });
  document.querySelector("#graph-zoom-in").addEventListener("click", () => graphController?.zoom(1.2), { signal });
  document.querySelector("#graph-zoom-out").addEventListener("click", () => graphController?.zoom(0.82), { signal });
  document.querySelector("#graph-reset").addEventListener("click", () => graphController?.reset(), { signal });
  signal.addEventListener("abort", () => graphController?.destroy(), { once: true });
  await load();
}

function renderSideData(graph, signal) {
  document.querySelector("#graph-count").textContent = `${graph.nodes.length} nodes · ${graph.edges.length} edges`;
  const types = [...new Set(graph.nodes.map(node => node.type))];
  document.querySelector("#graph-legend").innerHTML = types.map((type, index) => `<span class="legend-item"><i data-node-color="${index}"></i>${escapeHtml(type)}</span>`).join("");
  const style = getComputedStyle(document.documentElement);
  const palette = ["--chart-1", "--chart-2", "--chart-3", "--chart-4", "--chart-5"].map(name => style.getPropertyValue(name).trim());
  document.querySelectorAll("[data-node-color]").forEach(item => item.style.setProperty("--node-color", palette[Number(item.dataset.nodeColor) % palette.length]));
  document.querySelector("#graph-filters").innerHTML = types.map(type => `<button class="tab" aria-selected="true" data-graph-type="${escapeHtml(type)}">${escapeHtml(type)}</button>`).join("") || emptyState("Sin categorías", "El scanner todavía no ha creado nodos.");
  document.querySelectorAll("[data-graph-type]").forEach(button => button.addEventListener("click", () => { const selected = button.getAttribute("aria-selected") === "true"; button.setAttribute("aria-selected", String(!selected)); graphController?.filter(button.dataset.graphType, !selected); }, { signal }));
  document.querySelector("#graph-history").innerHTML = graph.nodes.slice(0, 8).map(node => `<div class="engine-row"><span>${escapeHtml(node.label)}</span>${stateBadge(node.type)}</div>`).join("") || emptyState("Sin historial", "Los nodos nuevos aparecerán aquí.");
}

function createGraph(canvas, graph, signal) {
  const context = canvas.getContext("2d");
  const style = getComputedStyle(document.documentElement);
  const palette = ["--chart-1", "--chart-2", "--chart-3", "--chart-4", "--chart-5"].map(name => style.getPropertyValue(name).trim());
  const typeIndex = new Map([...new Set(graph.nodes.map(node => node.type))].map((type, index) => [type, index]));
  const nodes = graph.nodes.map((node, index) => ({ ...node, x: Math.cos(index * 2.399) * Math.sqrt(index + 1) * 31, y: Math.sin(index * 2.399) * Math.sqrt(index + 1) * 31, visible: true }));
  const byId = new Map(nodes.map(node => [node.id, node]));
  let scale = 1, offsetX = 0, offsetY = 0, dragging = false, pointer = null, dragStart = null, destroyed = false;

  function resize() { const rect = canvas.getBoundingClientRect(); const ratio = window.devicePixelRatio || 1; canvas.width = Math.max(1, Math.floor(rect.width * ratio)); canvas.height = Math.max(1, Math.floor(rect.height * ratio)); draw(); }
  function draw() {
    if (destroyed) return;
    const ratio = window.devicePixelRatio || 1, width = canvas.clientWidth, height = canvas.clientHeight;
    context.setTransform(ratio, 0, 0, ratio, 0, 0); context.clearRect(0, 0, width, height); context.save(); context.translate(width / 2 + offsetX, height / 2 + offsetY); context.scale(scale, scale);
    context.strokeStyle = style.getPropertyValue("--color-border-strong").trim(); context.lineWidth = 0.7 / scale;
    graph.edges.forEach(edge => { const from = byId.get(edge.source), to = byId.get(edge.target); if (!from?.visible || !to?.visible) return; context.beginPath(); context.moveTo(from.x, from.y); context.lineTo(to.x, to.y); context.stroke(); });
    nodes.filter(node => node.visible).forEach(node => { const color = palette[typeIndex.get(node.type) % palette.length]; context.beginPath(); context.fillStyle = color; context.shadowColor = color; context.shadowBlur = 12 / scale; context.arc(node.x, node.y, Math.min(11, 4 + Math.log2(node.frequency + 1)), 0, Math.PI * 2); context.fill(); context.shadowBlur = 0; });
    context.restore();
  }
  function nodeAt(event) { const rect = canvas.getBoundingClientRect(), x = (event.clientX - rect.left - canvas.clientWidth / 2 - offsetX) / scale, y = (event.clientY - rect.top - canvas.clientHeight / 2 - offsetY) / scale; return nodes.filter(node => node.visible).find(node => Math.hypot(node.x - x, node.y - y) < 12 / scale); }
  function inspect(node) { const edges = graph.edges.filter(edge => edge.source === node.id || edge.target === node.id); document.querySelector("#graph-summary").textContent = `${node.type} · ${edges.length} relaciones`; document.querySelector("#node-detail").innerHTML = `<dl><dt>Nombre</dt><dd>${escapeHtml(node.label)}</dd><dt>Tipo</dt><dd>${escapeHtml(node.type)}</dd><dt>Frecuencia</dt><dd>${node.frequency}</dd><dt>Conexiones</dt><dd>${edges.length}</dd><dt>Actualizado</dt><dd>${new Date(node.updatedAt).toLocaleString()}</dd></dl><div class="node-history">${edges.slice(0, 8).map(edge => `<div class="engine-row"><span>${escapeHtml(edge.relation)}</span><small>${escapeHtml(edge.source === node.id ? edge.target : edge.source)}</small></div>`).join("")}</div>`; }
  const onPointerDown = event => { dragging = true; pointer = { x: event.clientX, y: event.clientY }; dragStart = { ...pointer }; canvas.setPointerCapture(event.pointerId); };
  const onPointerMove = event => { if (!dragging) return; offsetX += event.clientX - pointer.x; offsetY += event.clientY - pointer.y; pointer = { x: event.clientX, y: event.clientY }; draw(); };
  const onPointerUp = event => { const moved = dragStart && Math.hypot(event.clientX - dragStart.x, event.clientY - dragStart.y) > 3; dragging = false; if (!moved) { const node = nodeAt(event); if (node) inspect(node); } };
  const onWheel = event => { event.preventDefault(); scale = Math.max(0.35, Math.min(3.2, scale * (event.deltaY < 0 ? 1.12 : 0.9))); draw(); };
  canvas.addEventListener("pointerdown", onPointerDown); canvas.addEventListener("pointermove", onPointerMove); canvas.addEventListener("pointerup", onPointerUp); canvas.addEventListener("wheel", onWheel, { passive: false });
  const observer = new ResizeObserver(resize); observer.observe(canvas); resize();
  return { zoom: factor => { scale = Math.max(0.35, Math.min(3.2, scale * factor)); draw(); }, reset: () => { scale = 1; offsetX = 0; offsetY = 0; draw(); }, filter: (type, visible) => { nodes.filter(node => node.type === type).forEach(node => { node.visible = visible; }); draw(); }, destroy: () => { destroyed = true; observer.disconnect(); canvas.removeEventListener("pointerdown", onPointerDown); canvas.removeEventListener("pointermove", onPointerMove); canvas.removeEventListener("pointerup", onPointerUp); canvas.removeEventListener("wheel", onWheel); } };
}
