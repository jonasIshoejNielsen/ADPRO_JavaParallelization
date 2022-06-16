public class PerfTest {
    private volatile int vCtr;
    private int ctr;

    public void vInc() {
        vCtr++;
    }

    public void inc() {
        ctr++;
    }

    public static void main(String[] args) {
        int[] input = {100_000_00, 1000_000_00, Integer.MAX_VALUE / 10};
        System.out.println("***VOLATILE EXPERIMENTS***");
        for (int n : input) {
            System.out.println("N=" + n);
            long vol = new Exp().volExp(n);
            System.out.println("Volatile time: " + vol);
            System.out.println("Volatile time per iteration: " + (float)
                    vol / n);
        }
        System.out.println("\n");
        System.out.println("***NON-VOLATILE EXPERIMENTS***");
        for (int n : input) {
            System.out.println("N=" + n);
            long nonVol = new Exp().nonVolExp(n);
            System.out.println("Non volatile time: " + nonVol);
            System.out.println("Non Volatile time per iteration: " +
                    (float) nonVol / n);
            System.out.println("\n");
        }
    }
}
    class Exp {
        public long volExp (int N) {
            PerfTest pt = new PerfTest(); Long start = System.nanoTime(); for (int i = 0; i < N; i++) {
                pt.vInc(); }
            return System.nanoTime()-start; }
        public long nonVolExp (int N) { PerfTest pt = new PerfTest(); Long start = System.nanoTime(); for (int i = 0; i < N; i++) {
            pt.inc(); }
            return System.nanoTime()-start; }
    }