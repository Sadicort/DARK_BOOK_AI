import { api } from "./api.js";
import { emit } from "./events.js";

class RealtimeBridge {
  constructor() {
    this.timer = null;
    this.inFlight = false;
    this.lastSequence = 0;
    this.interval = 3000;
  }

  start() {
    if (this.timer) return;
    this.refresh();
    this.timer = window.setInterval(() => this.refresh(), this.interval);
  }

  stop() {
    if (this.timer) window.clearInterval(this.timer);
    this.timer = null;
  }

  async refresh() {
    if (this.inFlight || document.hidden) return;
    this.inFlight = true;
    const [dashboard, status, events] = await Promise.allSettled([api.dashboard(), api.status(), api.events(80)]);
    if (dashboard.status === "fulfilled") emit("realtime:dashboard", dashboard.value);
    if (status.status === "fulfilled") emit("realtime:status", status.value);
    if (events.status === "fulfilled") {
      const fresh = events.value.filter(event => event.sequence > this.lastSequence);
      if (events.value.length) this.lastSequence = Math.max(this.lastSequence, ...events.value.map(event => event.sequence));
      emit("realtime:events", { all: events.value, fresh });
    }
    if ([dashboard, status, events].every(result => result.status === "rejected")) emit("realtime:error", { message: "No se pudo sincronizar con la API local." });
    this.inFlight = false;
  }
}

export const realtime = new RealtimeBridge();

