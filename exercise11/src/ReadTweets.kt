import java.io.File
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel

fun main() = runBlocking<Unit>{
    val channel = Channel<Int>()
    launch {
        // this might be heavy CPU-consuming computation or async logic, we'll just send five squares
        for (x in 1..5) channel.send(x * x)
    }
    // here we print five received integers:
    repeat(5) { println(channel.receive()) }
    println("Done!")
}
/*
fun main() = runBlocking{
    //11.1
    System.out.println(readLines().count());
    //11.2
    System.out.println (readLines().map { x -> tweetFromLine(x) }.filter { x -> x != null }.count());

    //11.3
    GlobalScope.launch {
        print("newer printed")
    }.start()
    val tweetChannel = Channel<Tweet>()
    var producer = producer(tweetChannel)
    var consumer = consumer(tweetChannel)
    consumer.start()
    producer.start()


    producer.join()
    consumer.cancelAndJoin()
}

fun producer(tweetChannel: Channel<Tweet>) : Job{
    print("test")
    var pro =
        GlobalScope.launch {
            test(tweetChannel)
        }
    print("test 4")
    return pro
}
suspend fun test (tweetChannel: Channel<Tweet>){
    print("test2")
    readLines().map { x -> tweetFromLine(x) }.filter { x -> x != null }.forEach { x ->
        tweetChannel.send(x!!)
        delay(5)
    }
}

fun consumer(tweetChannel: Channel<Tweet>) : Job{
    var con =
        GlobalScope.launch {
            var i=0
            while(true){
                if(i%200 == 0){
                    print(tweetChannel.receive().text)
                }
                i += 1
            }
        }
    return con
}

fun readLines(): Sequence<String> = sequence<String> {
    val aPlaceOnYourDisk = "src/AirlineTweets2015.csv"
    val file = File( aPlaceOnYourDisk )
    val reader = file.bufferedReader()
    var line:String? = reader.readLine()
    while (line != null) {
        yield( line )
        line = reader.readLine()
    }
}

fun tweetFromLine(line: String): Tweet? {
    val cols : List<String> = line.split(",")
    if (cols.size == 15)
        return Tweet(cols[0], cols[1],cols[2],cols[3],cols[4],cols[5],cols[6],cols[7],
            cols[8],cols[9],cols[10],cols[11],cols[12],cols[13],cols[14])
    else
        return null
}

data class Tweet (
    var tweet_id : String,
    var airline_sentiment : String,
    var airline_sentiment_confidence : String,
    var negativereason : String,
    var negativereason_confidence : String,
    var airline : String,
    var airline_sentiment_gold : String,
    var name : String,
    var negativereason_gold : String,
    var retweet_count : String,
    var text : String,
    var tweet_coord : String,
    var tweet_created : String,
    var tweet_location : String,
    var user_timezone : String
)
*/