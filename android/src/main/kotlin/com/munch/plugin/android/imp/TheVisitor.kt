package com.munch.plugin.android.imp

import com.android.build.api.instrumentation.ClassData
import com.munch.plugin.android.Config
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes.*
import org.objectweb.asm.Type
import org.objectweb.asm.commons.AdviceAdapter


class TheMethodVisitor(
    api: Int,
    methodVisitor: MethodVisitor,
    access: Int,
    name: String,
    descriptor: String?,
    val classData: ClassData
) : AdviceAdapter(api, methodVisitor, access, name, descriptor) {

    private val task = mutableListOf<ASMTask>()

    override fun visitAnnotation(descriptor: String?, visible: Boolean): AnnotationVisitor {
        when (descriptor) {
            "Lcom/munch/plugin/annotation/Caller;" -> add(CallAsmTask())
            "Lcom/munch/plugin/annotation/Measure;" -> add(MeasureAsmTask())
        }
        return super.visitAnnotation(descriptor, visible)
    }

    override fun onMethodEnter() {
        super.onMethodEnter()
        if (task.isNotEmpty()) {
            task.forEach { it.executeEnter(mv, this) }
        }
    }

    override fun onMethodExit(opcode: Int) {
        super.onMethodExit(opcode)
        if (task.isNotEmpty()) {
            task.forEach { it.executeExit(mv, this) }
        }
    }

    private fun add(task: ASMTask) {
        this.task.add(task)
        this.task.sortByDescending { it.order }
    }
}

interface ASMTask {
    val order: Int
    fun executeEnter(mv: MethodVisitor, adapter: TheMethodVisitor)
    fun executeExit(mv: MethodVisitor, adapter: TheMethodVisitor)
}

class MeasureAsmTask : ASMTask {
    override val order: Int = 100
    private var startIndex: Int = 0
    private val tag = Config.tagCost
    private val minTime: Long = Config.minTime

    override fun executeEnter(mv: MethodVisitor, adapter: TheMethodVisitor) {
        startIndex = adapter.newLocal(Type.LONG_TYPE)
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J", false)
        mv.visitVarInsn(LSTORE, startIndex)
    }

    override fun executeExit(mv: MethodVisitor, adapter: TheMethodVisitor) {
        //设置地址变量
        val endIndex = adapter.newLocal(Type.LONG_TYPE)
        val costIndex = adapter.newLocal(Type.LONG_TYPE)
        //调用System.currentTimeMillis
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J", false)
        //存放System.currentTimeMillis的结果
        mv.visitVarInsn(LSTORE, endIndex)

        //读取两个时间
        mv.visitVarInsn(LLOAD, endIndex)
        mv.visitVarInsn(LLOAD, startIndex)
        //并相减
        mv.visitInsn(LSUB)
        //并保存
        mv.visitVarInsn(LSTORE, costIndex)

        //加载两个时间
        mv.visitVarInsn(LLOAD, costIndex)
        mv.visitLdcInsn(minTime)
        //并对两long进行判断, 得到结果(1,0,-1)
        mv.visitInsn(LCMP)
        val label = Label()
        // if语句要与 visitLabel 对应
        //若栈顶int类型小于等于0则跳转
        mv.visitJumpInsn(IFLE, label)

        //加载Log.d的第一个参数tag
        mv.visitLdcInsn(tag)

        //加载Log.d的第二个参数
        mv.visitTypeInsn(NEW, "java/lang/StringBuilder")
        mv.visitInsn(DUP)
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false)

        mv.visitLdcInsn("${adapter.classData.className}.${adapter.name} cost ")
        mv.visitMethodInsn(
            INVOKEVIRTUAL,
            "java/lang/StringBuilder",
            "append",
            "(Ljava/lang/String;)Ljava/lang/StringBuilder;",
            false
        )

        mv.visitVarInsn(LLOAD, costIndex)
        mv.visitMethodInsn(
            INVOKEVIRTUAL,
            "java/lang/StringBuilder",
            "append",
            "(J)Ljava/lang/StringBuilder;",
            false
        )

        mv.visitLdcInsn(" ms.")
        mv.visitMethodInsn(
            INVOKEVIRTUAL,
            "java/lang/StringBuilder",
            "append",
            "(Ljava/lang/String;)Ljava/lang/StringBuilder;",
            false
        )

        mv.visitMethodInsn(
            INVOKEVIRTUAL,
            "java/lang/StringBuilder",
            "toString",
            "()Ljava/lang/String;",
            false
        )

        mv.visitMethodInsn(
            INVOKESTATIC,
            "android/util/Log",
            "d",
            "(Ljava/lang/String;Ljava/lang/String;)I",
            false
        )

        mv.visitInsn(POP)
        mv.visitLabel(label)
    }
}

class CallAsmTask : ASMTask {
    override val order: Int = 0
    private val tag = Config.tagCall
    override fun executeEnter(mv: MethodVisitor, adapter: TheMethodVisitor) {

        mv.visitLdcInsn(tag)
        mv.visitLdcInsn("${adapter.classData.className}.${adapter.name} called.")
        mv.visitMethodInsn(
            INVOKESTATIC,
            "android/util/Log",
            "d",
            "(Ljava/lang/String;Ljava/lang/String;)I",
            false
        )
    }

    override fun executeExit(mv: MethodVisitor, adapter: TheMethodVisitor) {
    }
}