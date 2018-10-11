// WITH_COROUTINES
// WITH_RUNTIME

import helpers.*
import kotlin.coroutines.*
import kotlin.coroutines.intrinsics.*

inline class BoxAny(val value: Any?) {
    val intValue: Int get() = value as Int
}

inline class BoxInt(val value: Int)

inline class BoxLong(val value: Long)

class EmptyContinuation<T> : Continuation<T> {
    override val context: CoroutineContext
        get() = EmptyCoroutineContext

    override fun resumeWith(result: Result<T>) {}
}

suspend fun foo(block: suspend (BoxAny) -> Unit) {
    block(BoxAny(1))
    block.startCoroutineUninterceptedOrReturn(BoxAny(1), EmptyContinuation())
}

suspend fun bar(block: suspend (BoxInt) -> Unit) {
    block(BoxInt(2))
    block.startCoroutineUninterceptedOrReturn(BoxInt(2), EmptyContinuation())
}

suspend fun baz(block: suspend (BoxLong) -> Unit) {
    block(BoxLong(3))
    block.startCoroutineUninterceptedOrReturn(BoxLong(3), EmptyContinuation())
}

class Scope

fun runBlocking(block: suspend () -> Unit) {
    block.startCoroutine(object : Continuation<Unit> {
        override val context: CoroutineContext
            get() = EmptyCoroutineContext

        override fun resumeWith(result: Result<Unit>) {
            (block as Function1<Continuation<Unit>, Any?>)(this)
        }
    })
}

fun box(): String {
    var result = 0
    runBlocking {
        foo { boxAny ->
            result += boxAny.intValue
        }
        bar { boxInt ->
            result += boxInt.value
        }
        baz { boxLong ->
            result += boxLong.value.toInt()
        }
    }

    return if (result == 24) "OK" else "Error: $result"
}