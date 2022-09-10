package com.munch.plugin.android

import com.android.build.api.instrumentation.FramesComputationMode
import com.android.build.api.instrumentation.InstrumentationScope
import com.android.build.api.variant.AndroidComponentsExtension
import com.munch.plugin.android.extension.PluginAndroidExtension
import com.munch.plugin.android.helper.catch
import com.munch.plugin.android.helper.log
import com.munch.plugin.android.imp.TheClassVisitorFactory
import org.gradle.api.Plugin
import org.gradle.api.Project

class PluginAndroid : Plugin<Project> {

    private companion object {
        const val TAG = "pluginAndroid"
    }

    override fun apply(p: Project) {
        log("> Apply Plugin Android :${p.name}")
        readConfig(p)
        setAndroidTransform(p)
    }

    private fun setAndroidTransform(p: Project) {
        val android = p.extensions.findByType(AndroidComponentsExtension::class.java)
        android?.onVariants {
            it.instrumentation.transformClassesWith(
                TheClassVisitorFactory::class.java,
                InstrumentationScope.PROJECT
            ) {}
            it.instrumentation.setAsmFramesComputationMode(FramesComputationMode.COMPUTE_FRAMES_FOR_INSTRUMENTED_METHODS)
        }
    }

    private fun readConfig(p: Project) {
        //p.plugins.apply()
        /*val pluginExtension = */p.extensions.create(TAG, PluginAndroidExtension::class.java)

        p.afterEvaluate {
            catch {
                val e: PluginAndroidExtension =
                    it.extensions.findByName(TAG) as PluginAndroidExtension

                e.getEnable().orNull?.let { Config.enable = it }
                e.getLogDebug().orNull?.let { Config.logDebug = it }
                e.getPackName().orNull?.let { Config.packName = it }
                e.getTagCost().orNull?.let { Config.tagCost = it }
                e.getMinTime().orNull?.let { Config.minTime = it }
                e.getTagCall().orNull?.let { Config.tagCall = it }
                log("> Read Plugin Android Config :${Config}")
            }
        }
    }
}
