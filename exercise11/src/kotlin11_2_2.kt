import kotlinx.coroutines.*
fun main() {
    test(1)
    test(10)
    test(20)
    test(50)
    test(100)
    test(500)
    test(1000)
    test(10000)
    test(100000)
}
fun test(tries: Int){
    var time = 0L;
    runBlocking {
        repeat(tries){
        	val start = System.nanoTime()
            launch {
        		time += System.nanoTime() - start
            }.join()
            
        }
        
    }
    println("Average time to start: "+tries+" %,d".format(time/tries))
}