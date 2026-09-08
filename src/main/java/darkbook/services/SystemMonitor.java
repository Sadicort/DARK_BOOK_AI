package darkbook.services;

import java.lang.management.ManagementFactory;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/** Samples JVM, memory, CPU and disk usage for the real-time performance panel. */
public final class SystemMonitor {
    private final Path root;

    public SystemMonitor(Path root) { this.root = root; }

    public Map<String, Object> snapshot() {
        Runtime runtime = Runtime.getRuntime();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("processors", runtime.availableProcessors());
        result.put("heapUsedBytes", runtime.totalMemory() - runtime.freeMemory());
        result.put("heapMaxBytes", runtime.maxMemory());
        result.put("javaVersion", System.getProperty("java.version"));
        result.put("uptimeMs", ManagementFactory.getRuntimeMXBean().getUptime());
        var os = ManagementFactory.getOperatingSystemMXBean();
        result.put("systemLoadAverage", Math.max(0, os.getSystemLoadAverage()));
        if (os instanceof com.sun.management.OperatingSystemMXBean extended) {
            result.put("cpuLoad", Math.max(0, extended.getCpuLoad()));
            result.put("totalMemoryBytes", extended.getTotalMemorySize());
            result.put("freeMemoryBytes", extended.getFreeMemorySize());
        }
        try {
            FileStore store = Files.getFileStore(root);
            result.put("diskTotalBytes", store.getTotalSpace()); result.put("diskFreeBytes", store.getUsableSpace());
        } catch (Exception ignored) { }
        return result;
    }
}

