// Week 3
// sestoft@itu.dk * 2015-09-09

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TestWordStream {
  public static void main(String[] args) {
    String filename = "english-words.txt";

    System.out.println(readWords(filename).count());
    //6.2.2
    System.out.println("");
    readWords(filename).limit(100).forEach(s-> System.out.println(s));
    //6.2.3
    System.out.println("");
    readWords(filename).filter(s-> s.length()>=22).forEach(s-> System.out.println(s));    //System.out::println
    System.out.println(readWords(filename).filter(s-> s.length()>=22).count());
    //6.2.4
    System.out.println("");
    System.out.println(readWords(filename).filter(s-> s.length()>=22).findAny().get());
    //6.2.5
    System.out.println("");
    readWords(filename).filter(s-> isPalindrome(s)).forEach(s-> System.out.println(s));
    System.out.println(readWords(filename).filter(s-> isPalindrome(s)).count());
    //6.2.6
    System.out.println("");
    readWords(filename).parallel().filter(s-> isPalindrome(s)).forEach(s-> System.out.println(s));
    System.out.println(readWords(filename).parallel().filter(s-> isPalindrome(s)).count());
    //6.2.7
    System.out.println("");
    var stat = readWords(filename).parallel().mapToInt(s-> s.length()).summaryStatistics();   //stat.get*
    System.out.println("Min: "+ readWords(filename).parallel().mapToInt(s-> s.length()).min().getAsInt()  );
    System.out.println("Max: "+ readWords(filename).parallel().mapToInt(s-> s.length()).max().getAsInt()  );
    System.out.println("Average: "+ readWords(filename).parallel().mapToInt(s-> s.length()).average().getAsDouble()  );
    //6.2.8
    System.out.println("");
    readWords(filename).collect(Collectors.groupingBy(s-> s.length())).forEach((s,lst)-> System.out.println(""+s+" "+lst.size()));
    //6.2.9
    System.out.println("");
    readWords(filename).limit(100).forEach(s-> System.out.println(letters(s)));
    //6.2.10
    System.out.println("");
    System.out.println(letters(readWords(filename).reduce("", (s1,s2)-> s1+s2)));
    System.out.println(readWords(filename).map(TestWordStream::letters).reduce(0, (acc,letters)-> acc + letters.getOrDefault('e', 0), (acc1,acc2)->acc1+acc2));
    //6.2.11
    System.out.println("");
    System.out.println(readWords(filename).collect(Collectors.groupingBy(s-> letters(s))).values().stream().filter(lst -> lst.size()>1).count());

    //6.2.12
    System.out.println("");
    System.out.println(readWords(filename).parallel().collect(Collectors.groupingBy(s-> letters(s))).values().parallelStream().filter(lst -> lst.size()>1).count());

    //6.2.13
    System.out.println("");
    System.out.println(readWords(filename).parallel().collect(Collectors.groupingByConcurrent(s-> letters(s))).values().parallelStream().filter(lst -> lst.size()>1).count());
  }

  public static Stream<String> readWords(String filename) {
    try {
      BufferedReader reader = new BufferedReader(new FileReader(filename));
      return reader.lines().flatMap(s-> Arrays.stream(s.split(" ")));
    } catch (IOException exn) { 
      return Stream.<String>empty();
    }
  }

  public static boolean isPalindrome(String s) {
    for(int i =0; i<s.length(); i++){
      if (s.charAt(i) != s.charAt(s.length()-1 - i)){
        return false;
      }
    }
    return true;
  }

  public static Map<Character,Integer> letters(String s) {
    Map<Character,Integer> res = new TreeMap<>();
    for (char c: s.toLowerCase().toCharArray()) {
      int curr = res.getOrDefault(c, 0);
      res.put(c, curr+1);
    }
    return res;
  }
}
