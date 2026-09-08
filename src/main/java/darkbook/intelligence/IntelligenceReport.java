package darkbook.intelligence;

public class IntelligenceReport {

    private int duplicatedVideos;
    private int adsSkipped;
    private int captchasFound;
    private int reloads;

    public void duplicated(){
        duplicatedVideos++;
    }

    public void adSkipped(){
        adsSkipped++;
    }

    public void captcha(){
        captchasFound++;
    }

    public void reload(){
        reloads++;
    }

    public void printReport(){

        System.out.println("\n===== TikTok Intelligence Report =====");

        System.out.println("Duplicados evitados : " + duplicatedVideos);

        System.out.println("Publicidad ignorada : " + adsSkipped);

        System.out.println("CAPTCHAs detectados : " + captchasFound);

        System.out.println("Recargas realizadas : " + reloads);

        System.out.println("======================================");

    }

}