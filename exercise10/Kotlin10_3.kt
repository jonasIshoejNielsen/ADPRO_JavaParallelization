fun main() {
    ifNotEmpty("test"){s->println(s)}
    ifNotEmpty(""){s->println(s)}
    ifNotEmpty(null){s->println(s)}
}
fun ifNotEmpty(s:String?, f: (String) -> Unit):Unit{
    var s2 = s ?: return
    if (s2 == "") return
    f(s2)
}


//10.3.3
fun main() {
    "test".ifNotEmpty{s->println(s)}
    "".ifNotEmpty{s->println(s)}
    null.ifNotEmpty{s->println(s)}
}
fun String?.ifNotEmpty(f: (String) -> Unit):Unit{
    var s2 = this ?: return
    if (s2 == "") return
    f(s2)
}