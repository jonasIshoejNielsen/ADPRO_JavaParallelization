import kotlinx.coroutines.*
fun main() {
    var timeAvg = 0L;
    runBlocking {
        for(i in 0..99){
            val start = System.nanoTime()
            launch {
                val time = System.nanoTime() - start
                timeAvg += time
                //println("Starttime: %,d".format(time))
            }
        }
    }
    println("Average time to start: "+100+" %,d".format(timeAvg/100))
}