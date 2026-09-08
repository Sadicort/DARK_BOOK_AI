package darkbook.scanner;

public class WatchTimer {

    // Tiempo mínimo que se observará un video
    private final int minimumWatchSeconds = 8;

    // Tiempo máximo permitido
    private final int maximumWatchSeconds = 15;

    public void watchVideo() {

        int watchTime = generateWatchTime();

        System.out.println("Observando video durante " + watchTime + " segundos...");

        try {

            Thread.sleep(watchTime * 1000L);

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

        }

    }

    private int generateWatchTime() {

        return minimumWatchSeconds +
                (int)(Math.random() *
                        (maximumWatchSeconds - minimumWatchSeconds + 1));

    }

}