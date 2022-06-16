// Solution to exercise 6.1 PCPP Fall 2020
// The solutions to the individual questions are at the end of the file
// Each solution is briefly tested at the end of the main program
// 
// Remember, there are many possible solutions to these exercises
// Yours might be as good as these - readability is an important quality

import java.util.function.BiFunction;
import java.util.function.Consumer;  
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.BinaryOperator;   

class FunListSolution {
  public static void main(String[] args) {
    FunList<Integer> empty = new FunList<>(null),
      list1 = cons(9, cons(13, cons(0, empty))),                  // 9 13 0       
      list2 = cons(7, list1),                                     // 7 9 13 0     
      list3 = cons(8, list1),                                     // 8 9 13 0     
      list4 = list1.insert(1, 12),                                // 9 12 13 0    
      list5 = list2.removeAt(3),                                  // 7 9 13       
      list6 = list5.reverse(),                                    // 13 9 7       
      list7 = list5.append(list5);                                // 7 9 13 7 9 13
    System.out.println(list1);
    System.out.println(list2);
    System.out.println(list3);
    System.out.println(list4);
    System.out.println(list5);
    System.out.println(list6);
    System.out.println(list7);
    FunList<Double> list8 = list5.map(i -> 2.5 * i);              // 17.5 22.5 32.5
    System.out.println(list8); 
    double sum = list8.reduce(0.0, (res, item) -> res + item),    // 72.5
       product = list8.reduce(1.0, (res, item) -> res * item);    // 12796.875
    System.out.println(sum);
    System.out.println(product);
    FunList<Boolean> list9 = list5.map(i -> i < 10);              // true true false 
    System.out.println(list9);
    boolean allBig = list8.reduce(true, (res, item) -> res && item > 10);
    System.out.println(allBig);
    
    // Test Q1
    FunList<Integer> nineRemoved = list2.remove(9);
    System.out.println("Q1 - should be 7, 13, 0: " + nineRemoved);
    
    // Test Q2
    System.out.println("Q2 - 2 elements larger than 8: " + list2.count( elem -> elem > 8 ));
    
    // Test Q3
    System.out.println("Q3 - 9,13 larger than 8: " + list2.filter( elem -> elem > 8 ));
    
    // Test Q4
    FunList<Integer> nineFunRemoved = list2.removeFun(9);
    System.out.println("Q4 - should be 7, 13, 0: " + nineFunRemoved);
    
    // Test Q5
    FunList<FunList<Integer>> unflat = new FunList<FunList<Integer>>();
    unflat = unflat.insert(0,list6);
    unflat = unflat.insert(0,list2);
    FunList<Integer> flattened = FunList.flatten(unflat);
    System.out.println("Q5 - should be 7 9 13 0 13 9 7: " + flattened);
    
    // Test Q6
    
    flattened = FunList.flattenFun(unflat);
    System.out.println("Q6 - should be 7 9 13 0 13 9 7: " + flattened);
    
    // Test 07
    FunList<Integer> flatMapped = list2.flatMap( i -> cons(i, cons(i*i,empty)) );
    System.out.println("Q7a - 7 49 9 81 13 169 0 0: " + flatMapped);
    flatMapped = list2.flatMapFun( i -> cons(i, cons(i*i,empty)) );
    System.out.println("Q7b - 7 49 9 81 13 169 0 0: " + flatMapped);
    
    // Test 08
    FunList<Integer> summed = list7.scan( (a,b) -> a+b );
    // List7 is: 7 9 13 7 9 13
    System.out.println("Q8 - should be 7 16 29 36 45 58: " + summed);
  }

  public static <T> FunList<T> cons(T item, FunList<T> list) { 
    return list.insert(0, item);
  }
  
}

class FunList<T> {
  final Node<T> first;

  protected static class Node<U> {
    public final U item;
    public final Node<U> next;

    public Node(U item, Node<U> next) {
      this.item = item; 
      this.next = next; 
    }
  }

  public FunList(Node<T> xs) {    
    this.first = xs;
  }

  public FunList() { 
    this(null);
  }


  public int getCount() {
    Node<T> xs = first;
    int count = 0;
    while (xs != null) {
      xs = xs.next;
      count++;
    }
    return count;
  }

  public T get(int i) {
    return getNodeLoop(i, first).item;
  }

  // Loop-based version of getNode
  protected static <T> Node<T> getNodeLoop(int i, Node<T> xs) {
    while (i != 0) {
      xs = xs.next;
      i--;
    }
    return xs;    
  }

  // Recursive version of getNode
  protected static <T> Node<T> getNodeRecursive(int i, Node<T> xs) {    // Could use loop instead
    return i == 0 ? xs : getNodeRecursive(i-1, xs.next);
  }

  public static <T> FunList<T> cons(T item, FunList<T> list) { 
    return list.insert(0, item);
  }

  public FunList<T> insert(int i, T item) { 
    return new FunList<T>(insert(i, item, this.first));
  }

  protected static <T> Node<T> insert(int i, T item, Node<T> xs) { 
    return i == 0 ? new Node<T>(item, xs) : new Node<T>(xs.item, insert(i-1, item, xs.next));
  }

  public FunList<T> removeAt(int i) {
    return new FunList<T>(removeAt(i, this.first));
  }

  protected static <T> Node<T> removeAt(int i, Node<T> xs) {
    return i == 0 ? xs.next : new Node<T>(xs.item, removeAt(i-1, xs.next));
  }
  
  public FunList<T> reverse() {
    Node<T> xs = first, reversed = null;
    while (xs != null) {
      reversed = new Node<T>(xs.item, reversed);
      xs = xs.next;      
    }
    return new FunList<T>(reversed);
  }

  public FunList<T> append(FunList<T> ys) {
    return new FunList<T>(append(this.first, ys.first));
  }

  protected static <T> Node<T> append(Node<T> xs, Node<T> ys) {
    return xs == null ? ys : new Node<T>(xs.item, append(xs.next, ys));
  }

  @Override
  @SuppressWarnings("unchecked")
  public boolean equals(Object that) {
    return equals((FunList<T>)that);             // Unchecked cast
  }

  public boolean equals(FunList<T> that) {
    return that != null && equals(this.first, that.first);
  }

  // Could be replaced by a loop
  protected static <T> boolean equals(Node<T> xs1, Node<T> xs2) {
    return xs1 == xs2 
        || xs1 != null && xs2 != null && xs1.item == xs2.item && equals(xs1.next, xs2.next);
  }

  public <U> FunList<U> map(Function<T,U> f) {
    return new FunList<U>(map(f, first));
  }

  protected static <T,U> Node<U> map(Function<T,U> f, Node<T> xs) {
    return xs == null ? null : new Node<U>(f.apply(xs.item), map(f, xs.next));
  }

  public <U> U reduce(U x0, BiFunction<U,T,U> op) {
    return reduce(x0, op, first);
  }

  // Could be replaced by a loop
  protected static <T,U> U reduce(U x0, BiFunction<U,T,U> op, Node<T> xs) {
    return xs == null ? x0 : reduce(op.apply(x0, xs.item), op, xs.next);
  }

  // This loop is an optimized version of a tail-recursive function 
  public void forEach(Consumer<T> cons) {
    Node<T> xs = first;
    while (xs != null) {
      cons.accept(xs.item);
      xs = xs.next;
    }
  }

  @Override 
  public String toString() {
    StringBuilder sb = new StringBuilder();
    forEach(item -> sb.append(item).append(" "));
    return sb.toString();
  }
  
  // Question 1
  public FunList<T> remove(T item){
    return new FunList<T>( remove (this.first, item));
  }
  protected static <T> Node<T> remove(Node<T> node, T item){
    if (node == null) return null;
    if (node.item == item) return remove( node.next, item);
    return new Node<T>(node.item, remove( node.next, item));
  }
  
  // Question 2
  public int count(Predicate<T> p){
    int count = 0;
    Node<T> xs = first;
    while (xs != null) {
      if ( p.test(xs.item) ) count++;
      xs = xs.next;
    }
    return count;
  }
  
  // Question 3
  public FunList<T> filter(Predicate<T> p){
    return new FunList<T>( filter (this.first, p));
  }
  protected static <T> Node<T> filter(Node<T> node, Predicate<T> p){
    if (node == null) return null;
    if ( p.test(node.item) ) 
      return new Node<T>(node.item, filter( node.next, p));
    return filter( node.next, p);
  }
  
  // Question 4
  public FunList<T> removeFun(T item){
    return filter( elem -> elem != item );
  }
  
  // Question 5
 public static <T> FunList<T> flatten(FunList<FunList<T>> xss){
   if (xss.first == null) return new FunList<T>();
   return xss.first.item.append( flatten( xss.removeAt(0) )  );
 }
 
 // Q6
 public static <T> FunList<T> flattenFun(FunList<FunList<T>> xss){
   return xss.reduce( new FunList<T>(), (flat, list) -> flat.append(list) );
 }
 
 // Q7
 public <U> FunList<U> flatMap(Function<T,FunList<U>> f){
   if (first == null) return new FunList<U>();
   if (first.next == null) return f.apply(first.item);
   return f.apply(first.item).append( this.removeAt(0).flatMap( f )  );
 }
 
 public <U> FunList<U> flatMapFun(Function<T,FunList<U>> f){
   return flatten(this.map(f));
 }
 
 // Q8
 public FunList<T> scan(BinaryOperator<T> f){
   return cons(first.item, removeAt(0).scanRec(first.item, f) );
 }
 
 protected FunList<T> scanRec(T prev, BinaryOperator<T> f){
   if (first == null) return this;
   T val = f.apply(prev, first.item);
   return cons(val, removeAt(0).scanRec(val, f));
 }
}

