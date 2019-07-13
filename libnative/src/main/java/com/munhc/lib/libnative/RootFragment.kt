package com.munhc.lib.libnative

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment

/**
 * Fragment在viewpager中的显示与隐藏：
 * 1、fragment新建流程会先调用[setUserVisibleHint]，然后调用生命周期，未缓存fragment时切换显示时也会触发该流程
 * 2、作为缓存fragment创建时，其生命周期会被调用，但此时未实际显示
 * 3、缓存的fragment相互切换时，生命周期不会调用，只会回调[setUserVisibleHint]
 *
 * Created by Munch on 2019/7/13 14:10
 */
open class RootFragment : Fragment(), INext {

    override fun getContext(): Context? {
        return super<Fragment>.getContext()
    }

    fun getArgument(): Bundle {
        return arguments ?: Bundle()
    }

    /**
     * 用于标记该Fragment已经生成，因为在新建Fragment的流程中[setUserVisibleHint]先于[onAttach]
     */
    private var isPrepared = false
    /**
     * 用于标记该fragment真正可见
     */
    private var isVisible2User = false

    /**
     * 在viewpager中，相邻fragment切换，生命周期不会回调，只会调用此方法
     */
    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        isVisible2User = isVisibleToUser
        judgeVisible2User()
        judgeInvisible2User()
    }

    /**
     * 在fragment跳转activity之和返回会回调[onStart]和[onResume],而不会回调[onCreateView]和[onViewCreated]
     * 所以isPrepared = true放在这里
     *
     * 在viewpager中，点击相邻Fragment如果是被重建，其生命周期会一直走到[onStart]，而此时[getUserVisibleHint]为false
     */
    override fun onStart() {
        super.onStart()
        isPrepared = true
        judgeVisible2User()
    }

    /**
     * 只有当可见的时候，变为不可见才调用[pauseLoad]，当作为相邻Fragment只调用生命周期时不调用[pauseLoad]
     */
    override fun onStop() {
        super.onStop()
        if (isVisible2User) {
            pauseLoad()
        }
    }

    open fun judgeVisible2User() {
        if (!isPrepared || !isVisible2User) {
            return
        }
        resumeLoad()
    }

    private fun judgeInvisible2User() {
        if (isPrepared && !isVisible2User) {
            pauseLoad()
        }
    }

    /**
     * 每次不可见都调用，注意调用时机
     */
    open fun pauseLoad() {
    }

    /**
     * 每次可见都调用，注意调用时机
     */
    open fun resumeLoad() {
    }

    /**
     * FragmentStatePagerAdapter和FragmentPagerAdapter在回收fragment时都会调用此方法
     */
    override fun onDestroyView() {
        super.onDestroyView()
        isPrepared = false
    }

}