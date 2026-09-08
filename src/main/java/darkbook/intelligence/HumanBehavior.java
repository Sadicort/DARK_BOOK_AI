package darkbook.intelligence;

import com.microsoft.playwright.Page;

import java.util.Random;

public class HumanBehavior {

    private final Page page;
    private final Random random = new Random();

    public HumanBehavior(Page page){
        this.page = page;
    }

    public void randomMouseMovement(){

        int x = random.nextInt(900) + 100;
        int y = random.nextInt(500) + 100;

        page.mouse().move(x, y);

        page.waitForTimeout(random.nextInt(400) + 100);

    }

    public void randomPause(){

        page.waitForTimeout(random.nextInt(1500) + 500);

    }

    public int randomScrollAmount(){

        return random.nextInt(500) + 900;

    }

}