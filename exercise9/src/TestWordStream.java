// Week 3
// sestoft@itu.dk * 2015-09-09

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import io.reactivex.*;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

public class TestWordStream {
  public static void main(String[] args) {
    String filename = "english-words.txt";
    Observable<String> lines = readWords(filename);
    Observer<String> display= new Observer<String>(){
      int printedWords = 0;
      @Override
      public void onSubscribe(Disposable disposable) { }

      @Override
      public void onNext(String s) {
        if (printedWords <100){
          System.out.println(s);
          printedWords++;
        }
      }

      @Override
      public void onError(Throwable throwable) {}

      @Override
      public void onComplete() {}
    };
    //9.5.1
    //lines.subscribe(display);

    //9.5.2
    /*
    lines.subscribe(line ->{
      if (line.length() >=22){
        System.out.println(line);
      }
    });
*/
    //9.5.3
    lines.subscribe(line ->{
      if (isPalindrome(line)){
        System.out.println(line);
      }
    });
  }

  public static Observable<String> readWords(String filename) {
    try {
      BufferedReader reader = new BufferedReader(new FileReader(filename));
      return Flowable.fromIterable(reader.lines()::iterator).toObservable();
    } catch (IOException exn) {
      System.out.println("IOException");
      return Observable.empty();
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
}
