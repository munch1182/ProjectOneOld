package com.munch.plugin.android.helper

import com.android.build.api.instrumentation.ClassData
import com.munch.plugin.annotation.Send
import org.objectweb.asm.Opcodes


fun <T> catch(any: () -> T?): T? {
    return try {
        any.invoke()
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun log(any: Any?) {
    println(any?.toString() ?: "null")
}

internal fun ClassData.fmt() =
    "$className, [${classAnnotations.joinToString()}], [${interfaces.joinToString()}]"

internal fun Int.api() = when (this) {
    Opcodes.ASM4 -> "ASM4"
    Opcodes.ASM5 -> "ASM5"
    Opcodes.ASM6 -> "ASM6"
    Opcodes.ASM7 -> "ASM7"
    Opcodes.ASM8 -> "ASM8"
    Opcodes.ASM9 -> "ASM9"
    else -> toString()
}

internal fun Int.access(): String {
    val sb = StringBuilder()
    when {
        this.has(Opcodes.ACC_PUBLIC) -> sb.append("public")
        this.has(Opcodes.ACC_PRIVATE) -> sb.append("private")
        this.has(Opcodes.ACC_PROTECTED) -> sb.append("protected")
        else -> {}
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

internal fun Int.has(any: Int) = this and any == any