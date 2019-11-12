import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

public class Car implements Runnable {
    private static int CARS_COUNT;
    static {
        CARS_COUNT = 0;
    }
    private Race race;
    private int speed;
    private String name;
    CountDownLatch carCdl;
    Semaphore smp;
    public static boolean winnerFinishedRace = false;

    public synchronized  static void checkIfIsTheWinner(String name){
        if(!winnerFinishedRace) {
            winnerFinishedRace = true;
            System.out.println("!!!!!В гонке победил " + name + "!!!!!");
        }
    }

    public String getName() {
        return name;
    }
    public int getSpeed() {
        return speed;
    }
    public Car(Race race, int speed, CountDownLatch carCdl, Semaphore smp ) {
        this.race = race;
        this.speed = speed;
        CARS_COUNT++;
        this.name = "Участник #" + CARS_COUNT;
        this.carCdl = carCdl;
        this.smp = smp;
    }

    @Override
    public void run() {
        try {
            System.out.println(this.name + " готовится");
            Thread.sleep(500 + (int)(Math.random() * 800));
            System.out.println(this.name + " готов");
            carCdl.countDown();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            carCdl.await();   // пока счетчик не приравняется нулю, будем стоять на этой строке
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < race.getStages().size(); i++) {
            if(race.getStages().get(i).getClass().getName().equals("Tunnel")){
                try {
                    System.out.println(name + " перед туннелем");
                    smp.acquire();
                    System.out.println(name + " заезжает в туннель");
                    race.getStages().get(i).go(this);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    System.out.println(name + " покинул туннель");
                    smp.release();
                }
            } else race.getStages().get(i).go(this);
            //System.out.println(race.getStages().get(i).getClass());
        }
        System.out.println(name + " завершил гонку");
        checkIfIsTheWinner(name);
    }
}
