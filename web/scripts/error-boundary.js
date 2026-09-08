(function installDarkBookErrorBoundary() {
  function report(message, source) {
    fetch("/api/frontend/log", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ level: "error", message: String(message || "Unknown frontend error").slice(0, 2000), source: String(source || "window").slice(0, 300) })
    }).catch(function ignoreLoggingFailure() {});
  }

  window.addEventListener("error", function onWindowError(event) {
    report(event.message, event.filename || "window.error");
  });

  window.addEventListener("unhandledrejection", function onUnhandledRejection(event) {
    var reason = event.reason && event.reason.message ? event.reason.message : event.reason;
    report(reason, "unhandledrejection");
  });
})();

