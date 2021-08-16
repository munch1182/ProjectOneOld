package com.munch.project.one.applib

import org.junit.Test
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

/**
 * Create by munch1182 on 2021/8/16 9:22.
 */
class MatcherTest {

    @Test
    fun test() {
        val compile = Pattern.compile("^.?\\d\\.\\d+")
        var str = "v0.2-17-ge2f11a3"
        var matcher = compile.matcher(str)
        println("matcher:$matcher}")
        if (matcher.find()) {
            println("matcher:${str.subSequence(matcher.start(), matcher.end())}")
        }

        str = "v0.21-17-ge2f11a3"
        matcher = compile.matcher(str)
        println("matcher:$matcher}")
        if (matcher.find()) {
            println("matcher:${str.subSequence(matcher.start(), matcher.end())}")
        }

        str = "a1.21-17-ge2f11a3"
        matcher = compile.matcher(str)
        println("matcher:$matcher}")
        if (matcher.find()) {
            println("matcher:${str.subSequence(matcher.start(), matcher.end())}")
        }

        println(SimpleDateFormat("yyyyMMddHHmmss").format(Date()))
    }
}