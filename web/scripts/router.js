import { emit } from "./events.js";

const routes = {
  dashboard: { title: "Dashboard", description: "Estado operativo del cerebro artificial." },
  scanner: { title: "Scanner", description: "Control de recolección y enriquecimiento de contenido." },
  intelligence: { title: "Intelligence Center", description: "Entrenamiento, embeddings, vocabulario e inferencia." },
  memory: { title: "Memory Center", description: "Exploración de memoria episódica, semántica y temporal." },
  graph: { title: "Knowledge Graph", description: "Mapa interactivo de entidades y relaciones aprendidas." },
  datasets: { title: "Dataset Manager", description: "Inventario y exploración de datos de entrenamiento." },
  models: { title: "Model Manager", description: "Modelos, checkpoints e historial de entrenamiento." },
  analytics: { title: "Analytics Center", description: "Tendencias, predicciones y actividad del sistema." },
  terminal: { title: "Dark Book Terminal", description: "Comandos internos, logs y diagnóstico seguro." },
  settings: { title: "Settings Center", description: "Configuración global persistente administrada por Java." }
};

let activeController = null;
let activeRoute = null;

function requestedRoute() {
  const value = window.location.hash.replace(/^#\/?/, "").split("?")[0];
  return routes[value] ? value : "dashboard";
}

export async function renderRoute() {
  const route = requestedRoute();
  if (route === activeRoute && activeController) return;
  activeController?.abort();
  activeController = new AbortController();
  activeRoute = route;
  const meta = routes[route];
  document.querySelector("#page-title").textContent = meta.title;
  document.querySelector("#page-description").textContent = meta.description;
  document.querySelector("#page-breadcrumb").textContent = route.toUpperCase();
  document.querySelectorAll("[data-route]").forEach(link => {
    if (link.dataset.route === route) link.setAttribute("aria-current", "page"); else link.removeAttribute("aria-current");
  });
  const content = document.querySelector("#page-content");
  const actions = document.querySelector("#page-actions");
  actions.replaceChildren();
  content.innerHTML = '<div class="loading-state"><div class="loader-ring"></div><strong>Sincronizando módulo</strong><span>Consultando servicios locales…</span></div>';
  try {
    const [pageResponse, module] = await Promise.all([fetch(`/pages/${route}.html`, { signal: activeController.signal }), import(`./${route}.js`)]);
    if (!pageResponse.ok) throw new Error(`No se encontró la vista ${route}.`);
    const html = await pageResponse.text();
    if (activeController.signal.aborted) return;
    content.innerHTML = html;
    content.classList.remove("page-enter");
    void content.offsetWidth;
    content.classList.add("page-enter");
    await module.mount({ signal: activeController.signal, actions });
    document.querySelector("#main-region").scrollTop = 0;
    emit("route:changed", { route });
  } catch (error) {
    if (activeController.signal.aborted) return;
    content.innerHTML = `<div class="error-state"><strong>No se pudo abrir ${meta.title}</strong><span>${escapeHtml(error.message)}</span><button id="route-retry" class="button">Reintentar</button></div>`;
    content.querySelector("#route-retry")?.addEventListener("click", () => { activeRoute = null; renderRoute(); }, { signal: activeController.signal });
  }
}

export function startRouter() {
  window.addEventListener("hashchange", renderRoute);
  return renderRoute();
}

export function currentRoute() { return activeRoute; }

function escapeHtml(value) {
  return String(value ?? "").replace(/[&<>'"]/g, character => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", "'": "&#39;", '"': "&quot;" })[character]);
}

