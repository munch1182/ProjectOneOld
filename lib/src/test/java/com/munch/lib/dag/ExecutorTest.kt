package com.munch.lib.dag

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Test

/**
 * Create by munch1182 on 2021/2/26 9:11.
 */
class ExecutorTest {

    @Test
    fun execute() {
        Executor()
            .add(Task4())
            .add(Task1())
            .add(Task3())
            .add(Task2())
            .add(Task5())
            .add(Task6())
            .execute()
    }

    @Test
    fun executeBlock() {
        Executor()
            .add(Task4())
            .add(Task1())
            .add(Task3())
            .add(Task7())
            .add(Task2())
            .add(Task5())
            .add(Task6())
            .setExecuteListener { task, _ -> println("${task.uniqueKey} executed") }
            .execute()
    }

    abstract class TestTask : Task() {
        override fun start(executor: Executor) {
            println("$uniqueKey start")
        }
    }

    class Task7 : TestTask() {

        override fun start(executor: Executor) {
            super.start(executor)
            signBlock()
            runBlocking {
                delay(5000L)
                next()
            }
        }

        override fun dependsOn(): MutableList<Key> {
            return mutableListOf()
        }

        override val dispatcher: CoroutineDispatcher
            get() = Dispatchers.IO

        override val uniqueKey: Key
            get() = Key("7")
    }

    class Task1 : TestTask() {

        override fun dependsOn(): MutableList<Key> {
            return mutableListOf()
        }

        override val dispatcher: CoroutineDispatcher
            get() = Dispatchers.IO

        override val uniqueKey: Key
            get() = Key("1")
    }

    class Task2 : TestTask() {
        override val uniqueKey: Key
            get() = Key("2")

        override fun dependsOn(): MutableList<Key> {
            return mutableListOf(Key("1"))
        }

        override val dispatcher: CoroutineDispatcher
            get() = Dispatchers.IO
    }

    class Task3 : TestTask() {
        override val uniqueKey: Key
            get() = Key("3")

        override fun dependsOn(): MutableList<Key> {
            return mutableListOf(Key("2"), Key("4"), Key("1"))
        }

        override val dispatcher: CoroutineDispatcher
            get() = Dispatchers.IO
    }

    class Task4 : TestTask() {
        override val uniqueKey: Key
            get() = Key("4")

        override fun dependsOn(): MutableList<Key> {
            return mutableListOf(Key("1"))
        }

        override val dispatcher: CoroutineDispatcher
            get() = Dispatchers.IO
    }

    class Task5 : TestTask() {
        override val uniqueKey: Key
            get() = Key("5")

        override fun dependsOn(): MutableList<Key> {
            return mutableListOf(Key("4"), Key("3"), Key("1"))
        }

        override val dispatcher: CoroutineDispatcher
            get() = Dispatchers.IO
    }

    class Task6 : TestTask() {
        override val uniqueKey: Key
            get() = Key("6")

        override fun dependsOn(): MutableList<Key> {
            return mutableListOf()
        }

        override val dispatcher: CoroutineDispatcher
            get() = Dispatchers.IO

        override fun getPriority() = 6
    }
}