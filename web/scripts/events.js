export const appEvents = new EventTarget();

export function emit(type, detail = {}) {
  appEvents.dispatchEvent(new CustomEvent(type, { detail }));
}

export function listen(type, handler, signal) {
  appEvents.addEventListener(type, handler, { signal });
}

export function notify(title, message, variant = "info") {
  emit("toast", { title, message, variant });
}

export function openModal(title, content, kicker = "DARK BOOK OS") {
  emit("modal", { title, content, kicker });
}

export function navigate(route) {
  window.location.hash = `#/${route}`;
}

