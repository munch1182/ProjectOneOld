package com.munch.project.testsimple.jetpack.model

/**
 * Create by munch1182 on 2020/12/18 15:39.
 */
data class Office(
    val children: List<Any>,
    val courseId: Int,
    val id: Int,
    val name: String,
    val order: Int,
    val parentChapterId: Int,
    val userControlSetTop: Boolean,
    val visible: Int
)