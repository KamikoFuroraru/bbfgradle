//File A.java
import kotlin.Metadata;

public final class A {
}


//File Main.kt
// IGNORE_BACKEND_FIR: JVM_IR
// IGNORE_BACKEND: JS_IR
// TODO: muted automatically, investigate should it be ran for JS or not
// IGNORE_BACKEND: JS, NATIVE

// WITH_REFLECT

import kotlin.test.assertEquals

fun box(): String {
    assertEquals("<init>", ::A.name)
    return "OK"
}

