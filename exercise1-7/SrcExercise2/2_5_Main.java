import java.util.Random;

class Main {
    public static void main(String[] args) {
        Random r = new Random();

        long startTime;
        long endTime;
        int loops = 100000000;

        testLong l = new testLong();
        testVolatileLong vl = new testVolatileLong();

        startTime = System.nanoTime();
        for(int i = 0; i < loops; i++){
            vl.vl = r.nextLong();
        }
        endTime = System.nanoTime();

        System.out.println("volatile " + (endTime - startTime) / loops);

        startTime = System.nanoTime();
        for(int i = 0; i < loops; i++){
            l.l = r.nextLong();
        }
        endTime = System.nanoTime();

        System.out.println("nonvolatile " + (endTime - startTime) / loops);
    }
}

class testLong{
    public long l;
}

class testVolatileLong{
    public volatile long vl;
}