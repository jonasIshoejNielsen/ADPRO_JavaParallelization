import java.util.Arrays;

public class primeNumberTheorem {

    public static void main(String[] args) {
        int N = 10000001;
        int[] a = new int[N];
        Arrays.setAll(a, i->isPrime(i) ? 1 : 0);    //6.3.1
        //System.out.println(Arrays.toString(a));
        Arrays.parallelPrefix(a, (v1,v2)->v1+v2 );  //6.3.2
        //System.out.println(Arrays.toString(a));
        System.out.println(a[10_000_000]);          //gives 664579

        for (int i = 0; i < N; i+=N/10) {
            var ratio = a[i]*Math.log(i) / i;
            System.out.println("");
            System.out.println(i);
            System.out.println(ratio);
        }

    }

    private static boolean isPrime(int n) {
        int k = 2;
        while (k * k <= n && n % k != 0)
            k++;
        return n >= 2 && k * k > n;
    }
}
