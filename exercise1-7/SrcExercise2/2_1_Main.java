import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

class Person {
    private long id;
    private String name;
    private int zip;
    private String address;

    private static long highest_id = 0;

    private Person() {
        id = Person.getAndIncrementNewID();
    }
    public static synchronized Person generate(){
        Person p = new Person();
        return p;
    }

    public static synchronized long getAndIncrementNewID() {
        return highest_id++;
    }
    public static synchronized long getHighest_id() {
        return highest_id;
    }

    public synchronized void setZipAddress(String address, int zip){
        this.address = address;
        this.zip=zip;
    }

    public synchronized long getId() {
        return id;
    }

    public synchronized String getName() {
        return name;
    }

    public synchronized int getZip() {
        return zip;
    }

    public synchronized String getAddress() {
        return address;
    }



    public static void main(String[] args){
        int n=100000;

        ConcurrentLinkedQueue<Person> persons=new ConcurrentLinkedQueue<>();
        ConcurrentLinkedQueue<Thread> threads=new ConcurrentLinkedQueue<>();
        for(int i=0;i<n; i++){
            Thread t1=new Thread(()->{
                persons.add(Person.generate());
            });
            threads.add(t1);
        }
        for(Thread thread:threads){thread.start();}
        try{
            for(Thread thread:threads){thread.join();}
        }
        catch(InterruptedException exn){
            System.out.println("Some thread was interrupted");
        }

        HashMap<Long, Boolean> hm=new HashMap<>();
        for(Person person:persons){
            hm.put(person.id,true);
        }
        System.out.println(hm.keySet().size());
    }
}
