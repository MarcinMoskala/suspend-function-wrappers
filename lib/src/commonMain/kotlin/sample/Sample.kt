package sample

expect class Sample() {
    fun checkMe(): Int
}

expect object Platform {
    val name: String
}

class A
class B: A()

fun b() = B()

fun hello(): String = "Hello from ${Platform.name}"