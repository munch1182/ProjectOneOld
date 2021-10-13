package com.munch.lib.base

/**
 * Create by munch1182 on 2021/10/13 10:22.
 */
class UnsupportedTypeException(override val message: String) : RuntimeException(message) {

    constructor() : this("Unsupported Type")
}

class UnKnowException(override val message: String) : RuntimeException(message) {

    constructor() : this("UnKnow")
}