package com.munch.plugin.annotation

@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.CLASS,
    AnnotationTarget.CONSTRUCTOR,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER
)
@Retention(AnnotationRetention.BINARY)
annotation class Measure

@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.CONSTRUCTOR,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER
)
@Retention(AnnotationRetention.BINARY)
annotation class Caller

object Send {

    var p: ((String) -> Unit)? = null
}

object Print {

    fun print(p: ((String) -> Unit)? = null) {
        Send.p = p
    }
}