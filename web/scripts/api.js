const JSON_HEADERS = { "Content-Type": "application/json" };

export async function request(path, options = {}) {
  const controller = new AbortController();
  const externalSignal = options.signal;
  const timeout = window.setTimeout(() => controller.abort(), 12000);
  const abortForwarder = () => controller.abort();
  externalSignal?.addEventListener("abort", abortForwarder, { once: true });
  const started = performance.now();
  try {
    const response = await fetch(`/api${path}`, {
      ...options,
      signal: controller.signal,
      headers: options.body ? { ...JSON_HEADERS, ...options.headers } : options.headers
    });
    const envelope = await response.json();
    window.dispatchEvent(new CustomEvent("darkbook:latency", { detail: Math.round(performance.now() - started) }));
    if (!response.ok || envelope.status !== "success") throw new Error(envelope.message || `HTTP ${response.status}`);
    return envelope.data;
  } catch (error) {
    if (error.name === "AbortError" && !externalSignal?.aborted) throw new Error("La API local tardó demasiado en responder.");
    throw error;
  } finally {
    window.clearTimeout(timeout);
    externalSignal?.removeEventListener("abort", abortForwarder);
  }
}

export const api = {
  status: signal => request("/status", { signal }),
  dashboard: signal => request("/dashboard", { signal }),
  events: (limit = 100, signal) => request(`/events?limit=${limit}`, { signal }),
  videos: (limit = 100, signal) => request(`/videos?limit=${limit}`, { signal }),
  scanner: signal => request("/scanner", { signal }),
  scannerAction: (action, signal) => request(`/scanner/${action}`, { method: "POST", body: "{}", signal }),
  memorySearch: (query, signal) => request(`/memory/search?q=${encodeURIComponent(query)}&limit=30`, { signal }),
  graph: signal => request("/graph?limit=350", { signal }),
  datasets: signal => request("/datasets", { signal }),
  models: signal => request("/models", { signal }),
  plugins: signal => request("/plugins", { signal }),
  training: signal => request("/training", { signal }),
  startTraining: signal => request("/training/start", { method: "POST", body: "{}", signal }),
  cancelTraining: signal => request("/training/cancel", { method: "POST", body: "{}", signal }),
  reason: (question, signal) => request("/reasoning", { method: "POST", body: JSON.stringify({ question }), signal }),
  infer: (text, signal) => request("/inference", { method: "POST", body: JSON.stringify({ text }), signal }),
  settings: signal => request("/settings", { signal }),
  saveSettings: (name, value, signal) => request(`/settings/${name}.json`, { method: "PUT", body: JSON.stringify(value), signal })
};

