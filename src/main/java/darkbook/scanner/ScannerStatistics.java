package darkbook.scanner;

public class ScannerStatistics {

    private int videosScanned = 0;
    private long startTime;
    private long endTime;

    public void start() {
        startTime = System.currentTimeMillis();
    }

    public void finish() {
        endTime = System.currentTimeMillis();
    }

    public void increaseVideos() {
        videosScanned++;
    }

    public int getVideosScanned() {
        return videosScanned;
    }

    public long getElapsedSeconds() {
        return (System.currentTimeMillis() - startTime) / 1000;
    }

    public long getElapsedMinutes() {
        return getElapsedSeconds() / 60;
    }

    public long getElapsedHours() {
        return getElapsedMinutes() / 60;
    }

    public long getTotalExecutionSeconds() {
        return (endTime - startTime) / 1000;
    }
}