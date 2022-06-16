fun main() {
    //10.4
    val seq1 = sequence{yieldAll(1940..2040 step 4)}
    println(seq1.toList())
    
    //10.5
    val seq2 = (1..40).asSequence()
    println(seq2.dropEverySecond().filter { v -> v%3 == 0 }.toList())
    
    //10.6
    val seq3 = leapYear(1940, 2040)
    println(seq3.dropEverySecond2().toList())
    
    //10.7
    val seq4 = merge((10..30 step 2).asSequence(), (11..31 step 2).asSequence()).toList()
    println(seq4.toList())
    
    
}

fun <T>Sequence<T>.dropEverySecond(): Sequence<T> = sequence {
    var iter = this@dropEverySecond.iterator()
    while(iter.hasNext()){
        yield (iter.next())        	
        if(iter.hasNext()){
            iter.next()
        }
    }
}

fun leapYear(from: Int, to: Int): Sequence<Int> = sequence {
    if(from<=to ){
        if (from % 4 == 0) yield (from)
        val rest = leapYear(from+1, to)
        yieldAll(rest)
    }
}

fun <T>Sequence<T>.dropEverySecond2(): Sequence<T> = sequence {
    if(any()){
        yield (first())
        val rest = filterIndexed{index, element -> index>=2}.dropEverySecond2()
        yieldAll(rest)
    }
}

fun merge(s1: Sequence<Int>, s2: Sequence<Int>): Sequence<Int> = sequence{
    //return (s1 + s2).sorted()
    val it1 = s1.toList().listIterator()
    val it2 = s2.toList().listIterator()
   	while(it1.hasNext() || it2.hasNext()){
        if(! it2.hasNext()){
            yieldAll(it1)
        }
        else if (! it1.hasNext()){
            yieldAll(it2)
        }
        else{
            val item1 = it1.next()
            val item2 = it2.next()
            if(item1<item2){
                yield(item1)
                it2.previous()
            }
            else {
                yield(item2)
                it1.previous()
            }
            
        }
    }
}










