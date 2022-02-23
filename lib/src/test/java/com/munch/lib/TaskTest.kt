package com.munch.lib

import com.munch.lib.log.log
import com.munch.lib.task.SimpleTask
import com.munch.lib.task.TaskHelper
import com.munch.lib.task.TaskKey
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Create by munch1182 on 2022/2/23 17:01.
 */
class TaskTest : BaseTest {

    @Test
    fun testTask() {
        TaskHelper()
            .add(TestTask2())
            .add(TestTask3())
            .add(TestTask5())
            .add(TestTask4())
            .add(TestTask1())
            .onSorted(object : TaskHelper.OnSortedListener {
                override fun onSorted(list: MutableList<TaskKey>) {
                    val str = list.joinToString()
                    log(str)
                    assertEquals("test1, test2, test3, test4, test5",str)
                }
            })
            .start()
    }

    class TestTask1 : TestTask("test1")

    class TestTask2 : TestTask("test2") {
        override fun getDependsTask() = mutableListOf(TaskKey("test1"))
    }

    class TestTask3 : TestTask("test3") {
        override fun getDependsTask() = mutableListOf(TaskKey("test2"))
    }

    class TestTask4 : TestTask("test4") {
        override fun getDependsTask() = mutableListOf(TaskKey("test2"), TaskKey("test3"))
    }

    class TestTask5 : TestTask("test5") {
        override fun getDependsTask() = mutableListOf(TaskKey("test1"), TaskKey("test4"))
    }

    open class TestTask(private val name: String) : SimpleTask {
        override fun run() {
            log("task:$key run")
        }

        override val key: TaskKey
            get() = TaskKey(name)


    }
}