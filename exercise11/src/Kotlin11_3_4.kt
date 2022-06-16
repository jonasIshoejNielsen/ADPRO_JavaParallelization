import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
fun main() {
    runBlocking {
        val measurement = Channel<Long>()
        val reps = 1000
        
        launch{ measurementCollector(reps, measurement) }
        
        runBlocking{
            for(i in 0..reps-1){
                launch {
                	val start = System.nanoTime()
                    measurement.send(start)
                }
            }
        }
        measurement.close()
    }
}
suspend fun measurementCollector(n: Int, measurements: Channel<Long>): Unit {
    var count = 0L
    var sum = 0L
    var ssm = 0L
    var exclude = 0L
    
    
    for (start in measurements) {
        if(exclude < n/10){
            exclude++
            continue
        }
        val time = System.nanoTime() - start
        count++
        sum += time
        ssm += time * time
    }
            
    val mean = (sum/count).toDouble()
    val sdev = Math.sqrt( (ssm - mean*mean*count).toDouble() )/(count-1)
    println("%,6.1f ns +/- %,8.2f %,d%n".format( mean, sdev, count) )
}