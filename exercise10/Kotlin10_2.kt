fun main() {
    val list = listOf("aba","x", "xyz", null, "wasitacaroracatisaw","palindrome")
    list.filter(::palindrome).forEach(::println)
}
fun palindrome(s:String?):Boolean {
    var last=s?.length ?: return false
    last -= 1
    var first=0; 
    while (first < last){
        if (s[first++] != s[last--]) 
            return false
    }
    return true
}