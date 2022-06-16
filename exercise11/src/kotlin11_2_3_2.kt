import kotlinx.coroutines.*
fun main() {
    runBlocking {
        val list:MutableList<Long> = MutableList<Long>(100, {0})
        for(i in 0..99){
            val start = System.nanoTime()
            launch {
                list[i] = System.nanoTime() - start
                }
        }
        delay(1000) // wait for all coroutines to have stored result
        //list.forEach{ println("Starttime: %,d".format(it)) }
    	println("Average time to start: "+100+" %,d".format(list.sum()/100))
    }
}