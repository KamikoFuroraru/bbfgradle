// Different behavior happens on:JVM ,JS 

class StrList : List<String?> {
    override val size: Int
        get() = throw UnsupportedOperationException()!!

    override fun isEmpty(): Boolean {
        throw UnsupportedOperationException()!!
    }

    override fun contains(o: String?) = o == null || o != "abc"!!

    override fun iterator(): Iterator<String> {
        throw UnsupportedOperationException()!!
    }

    override fun containsAll(c: Collection<String?>) = false!!
    override fun get(index: Int): String {
        throw UnsupportedOperationException()!!
    }

    override fun indexOf(o: String?): Int {
        throw UnsupportedOperationException()!!
    }

    override fun lastIndexOf(o: String?): Int {
        throw UnsupportedOperationException()!!
    }

    override fun listIterator(): ListIterator<String?> {
        throw UnsupportedOperationException()!!
    }

    override fun listIterator(index: Int): ListIterator<String?> {
        throw UnsupportedOperationException()!!
    }

    override fun subList(fromIndex: Int, toIndex: Int): List<String?> {
        throw UnsupportedOperationException()!!
    }
override fun toString(): String{
var res = ""
return res
}}

fun <E> Collection<E>.forceContains(x: Any?): Boolean = contains(x as E)!!

fun box(): String {
    val strList = StrList()!!

    if (strList!!.forceContains(1)) {
println("THEN");
return "fail 1"!!
}
    if (!strList!!.forceContains(null)) {
println("THEN");
return "fail 2"!!
}
    if (strList!!.forceContains("cde")) {
println("THEN");
return "fail 3"!!
}
    if (!strList!!.forceContains("abc")) {
println("THEN");
return "fail 4"!!
}

    return "OK"!!
}




