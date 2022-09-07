package com.munch.plugin.android.imp

import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.commons.AdviceAdapter

class TheMethodVisitor(
    api: Int,
    methodVisitor: MethodVisitor?,
    access: Int,
    name: String?,
    descriptor: String?
) : AdviceAdapter(api, methodVisitor, access, name, descriptor) {

    override fun onMethodEnter() {
        super.onMethodEnter()
    }

    override fun onMethodExit(opcode: Int) {
        super.onMethodExit(opcode)
    }
}