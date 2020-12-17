package com.munch.lib.test.recyclerview

import android.app.Activity
import android.os.Parcel
import android.os.Parcelable

/**
 * Create by munch1182 on 2020/12/7 13:58.
 */
data class TestRvItemBean(val name: String = "", var info: String) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    )

    @Suppress("UNCHECKED_CAST")
    fun getTarget(): Class<out Activity>? {
        if (info.isEmpty()) {
            return null
        }
        return Class.forName(info) as Class<out Activity>
    }

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeString(name)
        dest?.writeString(info)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<TestRvItemBean> {
        override fun createFromParcel(parcel: Parcel): TestRvItemBean {
            return TestRvItemBean(parcel)
        }

        override fun newArray(size: Int): Array<TestRvItemBean?> {
            return arrayOfNulls(size)
        }

        fun newInstance(name: String, clazz: Class<out Activity>? = null) =
            TestRvItemBean(name, clazz?.name ?: "")

        fun newArray(vararg clazz: Class<out Activity>): ArrayList<TestRvItemBean> {
            return ArrayList(MutableList(clazz.size, init = {
                val target = clazz[it]
                newInstance(target.simpleName.replace("Test", "").replace("Activity", ""), target)
            }))
        }

        /**
         * 配合isBtn使用
         */
        fun newArray(vararg name: String): ArrayList<TestRvItemBean> {
            return ArrayList(MutableList(name.size, init = {
                newInstance(name[it])
            }))
        }
    }

}
