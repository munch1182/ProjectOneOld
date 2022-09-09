package com.munch.plugin.android.imp

import com.android.build.api.instrumentation.AsmClassVisitorFactory
import com.android.build.api.instrumentation.ClassContext
import com.android.build.api.instrumentation.ClassData
import com.android.build.api.instrumentation.InstrumentationParameters
import com.munch.plugin.android.Config
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

abstract class TheClassVisitorFactory : AsmClassVisitorFactory<InstrumentationParameters.None> {

    override fun createClassVisitor(
        classContext: ClassContext,
        nextClassVisitor: ClassVisitor
    ): ClassVisitor {
        val api = instrumentationContext.apiVersion.getOrElse(Opcodes.ASM9)

        /*if (Config.log) {
            log("> createClassVisitor: ${api.api()}, ${classContext.currentClassData.fmt()}")
        }*/
        return TheVisitor(api, nextClassVisitor, classContext.currentClassData)
    }

    override fun isInstrumentable(classData: ClassData): Boolean {
        //判断类是否需要createClassVisitor
        val ok = classData.className.let {
            Config.packName.find { p -> it.startsWith(p) } != null
                    && !it.contains("R$")
                    && !it.endsWith(".R")
        }
        /*if (Config.log && ok) {
            log("> isInstrumentable: $ok -> (${classData.fmt()})")
        }*/
        return ok
    }

}

class TheVisitor(api: Int, next: ClassVisitor, private val classData: ClassData) :
    ClassVisitor(api, next) {

    override fun visitAnnotation(descriptor: String?, visible: Boolean): AnnotationVisitor {
        //log("> visit class Annotation: $descriptor, $visible")
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
        /*if (Config.log) {
            log("> visitMethod: $isNeed -> (${access.access()}, $name, $descriptor, $signature)")
        }*/
        if (isNeed) {
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
