import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StreamExtender {
    public static void main(String[] args) {
        Stream test1 = Stream.of(20,40,3,5,1,5,7,2,5,7,2,1,1,5);
        System.out.println(Arrays.toString(test1.toArray()));
        Stream test2 = Stream.of(20,40,3,5,1,5,7,2,5,7,2,1,1,5);
        System.out.println(Arrays.toString(dropEverySecond(test2).toArray()));
    }
    public static Stream dropEverySecond(Stream st){
        AtomicInteger index = new AtomicInteger();
        return st.filter(n -> index.incrementAndGet() % 2 != 0);
    }
}
