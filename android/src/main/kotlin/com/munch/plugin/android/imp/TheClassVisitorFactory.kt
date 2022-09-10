package com.munch.plugin.android.imp

import com.android.build.api.instrumentation.AsmClassVisitorFactory
import com.android.build.api.instrumentation.ClassContext
import com.android.build.api.instrumentation.ClassData
import com.android.build.api.instrumentation.InstrumentationParameters
import com.munch.plugin.android.Config
import com.munch.plugin.android.helper.access
import com.munch.plugin.android.helper.api
import com.munch.plugin.android.helper.fmt
import com.munch.plugin.android.helper.log
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

abstract class TheClassVisitorFactory : AsmClassVisitorFactory<InstrumentationParameters.None> {

    override fun isInstrumentable(classData: ClassData): Boolean {
        //判断类是否需要createClassVisitor
        val ok = classData.className.let {
            Config.packName.find { p -> it.startsWith(p) } != null
                    && !it.contains("R$")
                    && !it.endsWith(".R")
        }

        if (Config.logDebug) {
            log("> isInstrumentable: $ok -> (${classData.fmt()})")
        }
        return ok
    }

    override fun createClassVisitor(
        classContext: ClassContext,
        nextClassVisitor: ClassVisitor
    ): ClassVisitor {
        val api = instrumentationContext.apiVersion.getOrElse(Opcodes.ASM9)

        if (Config.logDebug) {
            log("> createClassVisitor: ${api.api()}, ${classContext.currentClassData.className}")
        }
        return TheVisitor(api, nextClassVisitor, classContext.currentClassData)
    }

}

class TheVisitor(api: Int, next: ClassVisitor, private val classData: ClassData) :
    ClassVisitor(api, next) {

    override fun visitAnnotation(descriptor: String?, visible: Boolean): AnnotationVisitor {
        if (Config.logDebug) {
            log("> visit class Annotation: $descriptor, $visible (${classData.className})")
        }
        return super.visitAnnotation(descriptor, visible)
    }

    override fun visitMethod(
        access: Int,
        name: String,
        descriptor: String?,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor {
        val visitor = super.visitMethod(access, name, descriptor, signature, exceptions)
        val isNeed = isNeedVisi(/*access, name, descriptor, signature*/)
        if (isNeed) {
            if (Config.logDebug) {
                log("> visitMethod: $isNeed -> (${access.access()} $descriptor $name${if (signature == null) "" else " $signature"}) (${classData.className})")
            }
            return TheMethodVisitor(api, visitor, access, name, descriptor, classData)
        }
        return visitor
    }

    private fun isNeedVisi(
        /*access: Int,
        name: String?,
        descriptor: String?,
        signature: String?*/
    ): Boolean {
        return true
    }
}
