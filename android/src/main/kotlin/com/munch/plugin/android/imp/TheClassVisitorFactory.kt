package com.munch.plugin.android.imp

import com.android.build.api.instrumentation.AsmClassVisitorFactory
import com.android.build.api.instrumentation.ClassContext
import com.android.build.api.instrumentation.ClassData
import com.android.build.api.instrumentation.InstrumentationParameters
import com.munch.plugin.android.Config
import com.munch.plugin.android.helper.log
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

abstract class TheClassVisitorFactory : AsmClassVisitorFactory<InstrumentationParameters.None> {

    override fun createClassVisitor(
        classContext: ClassContext,
        nextClassVisitor: ClassVisitor
    ): ClassVisitor {
        val api = instrumentationContext.apiVersion.getOrElse(Opcodes.ASM8)

        if (Config.log) {
            log("> createClassVisitor: ${api.api()}, ${classContext.currentClassData.fmt()}")
        }
        return TheVisitor(api, nextClassVisitor)
    }

    override fun isInstrumentable(classData: ClassData): Boolean {
        //判断类是否需要createClassVisitor
        val ok = classData.className.let {
            it.contains("com.munch")
                    && !it.contains("R$")
                    && !it.endsWith(".R")
        }
        if (Config.log && ok) {
            log("> isInstrumentable: $ok -> (${classData.fmt()})")
        }
        return ok
    }

}

class TheVisitor(api: Int, next: ClassVisitor) : ClassVisitor(api, next) {

    override fun visitMethod(
        access: Int,
        name: String?,
        descriptor: String?,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor {
        val visitor = super.visitMethod(access, name, descriptor, signature, exceptions)
        val isNeed = isNeedVisi(/*access, name, descriptor, signature*/)
        if (Config.log) {
            log("> visitMethod: $isNeed -> (${access.access()}, $name, $descriptor, $signature)")
        }
        if (isNeed) {
            return TheMethodVisitor(api, visitor, access, name, descriptor)
        }
        return visitor
    }

    private fun isNeedVisi(
        /*access: Int,
        name: String?,
        descriptor: String?,
        signature: String?*/
    ): Boolean {
        //需要以添加判断
        return true
    }
}

private fun ClassData.fmt() =
    "$className, [${classAnnotations.joinToString()}], [${interfaces.joinToString()}]"

private fun Int.api() = when (this) {
    Opcodes.ASM4 -> "ASM4"
    Opcodes.ASM5 -> "ASM5"
    Opcodes.ASM6 -> "ASM6"
    Opcodes.ASM7 -> "ASM7"
    Opcodes.ASM8 -> "ASM8"
    Opcodes.ASM9 -> "ASM9"
    else -> toString()
}

private fun Int.access(): String {
    val sb = StringBuilder()
    when {
        this.has(Opcodes.ACC_PUBLIC) -> sb.append("public")
        this.has(Opcodes.ACC_PRIVATE) -> sb.append("private")
        this.has(Opcodes.ACC_PROTECTED) -> sb.append("protected")
        else -> toString()
    }
    if (this.has(Opcodes.ACC_SYNCHRONIZED)) {
        sb.append(" ")
        sb.append("synchronized")
    }
    if (this.has(Opcodes.ACC_STATIC)) {
        sb.append(" ")
        sb.append("static")
    }
    if (this.has(Opcodes.ACC_FINAL)) {
        sb.append(" ")
        sb.append("final")
    }
    if (this.has(Opcodes.ACC_NATIVE)) {
        sb.append(" ")
        sb.append("native")
    }
    if (this.has(Opcodes.ACC_ANNOTATION)) {
        sb.append(" ")
        sb.append("annotation")
    } else if (this.has(Opcodes.ACC_ENUM)) {
        sb.append(" ")
        sb.append("enum")
    } else if (this.has(Opcodes.ACC_INTERFACE)) {
        sb.append(" ")
        sb.append("enum")
    }
    return sb.toString()
}

private fun Int.has(any: Int) = this and any == any
