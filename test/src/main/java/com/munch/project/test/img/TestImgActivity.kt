package com.munch.project.test.img

import android.Manifest
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.munch.lib.BaseApp
import com.munch.lib.helper.BarHelper
import com.munch.lib.helper.FileHelper
import com.munch.lib.helper.ImgHelper
import com.munch.lib.helper.ResultHelper
import com.munch.lib.test.TestDialog
import com.munch.project.test.BaseActivity
import com.munch.project.test.BaseFragment
import com.munch.project.test.R
import com.munch.project.test.img.camera.TestCameraActivity
import java.io.File

/**
 * Create by munch1182 on 2020/12/31 20:58.
 */
class TestImgActivity : BaseActivity() {

    private val btn: Button by lazy { findViewById(R.id.test_img_btn) }
    private val vp: ViewPager2 by lazy { findViewById(R.id.test_img_vp) }
    private val adapter by lazy { ViewPagerAdapter(this, count) }
    private val barHelper by lazy { BarHelper(this) }
    private val itemDecoration by lazy { BgItemDecoration(this) }
    private var bgFile: File? = null

    private var count = 5

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        barHelper.hideStatusBar(true).colorStatusBar(Color.TRANSPARENT)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            barHelper.setTextColorBlack(true)
        }
        setContentView(R.layout.activity_test_img)

        vp.adapter = adapter
        vp.offscreenPageLimit = count

        updateDefBg()

        btn.setOnClickListener {
            TestDialog.bottom(this)
                .addItems("chose from album", "open system camera", "open custom camera", "crop")
                .setOnClickListener { dialog, pos ->
                    if (pos == 3) {
                        if (bgFile == null) {
                            toast("需要先选择一个背景")
                        } else {
                            imgCrop(bgFile!!)
                        }
                    } else {
                        val permission: String
                        val type: Int
                        when (pos) {
                            0 -> {
                                permission = Manifest.permission.READ_EXTERNAL_STORAGE
                                type = 0
                            }
                            1 -> {
                                permission = Manifest.permission.CAMERA
                                type = 1
                            }
                            else -> {
                                permission = Manifest.permission.CAMERA
                                type = 2
                            }
                        }
                        ResultHelper.with(this)
                            .requestPermission(permission)
                            .res { allGrant, _, _ ->
                                if (allGrant) {
                                    imageCapture(type)
                                } else {
                                    toast("拒绝了权限")
                                }
                            }
                    }
                    dialog.cancel()
                }
                .show()
        }
    }

    private fun imageCapture(type: Int) {
        val file = File(filesDir, "img_bg.jpeg")
        val intent = when (type) {
            0 -> {
                ImgHelper.albumIntent()
            }
            1 -> {
                ImgHelper.imageCaptureIntent(this, file)
            }
            else -> {
                TestCameraActivity.intent(this, file)
            }
        }
        ResultHelper.with(this)
            .startForResult(intent)
            .res { isOk: Boolean, resCode: Int, data: Intent? ->
                if (isOk) {
                    val fileTemp = if (type == 0) {
                        val uri = data?.data ?: return@res
                        FileHelper.uri2File(
                            BaseApp.getInstance(),
                            uri,
                            File(filesDir, "img_from_uri.jpeg")
                        )
                    } else {
                        file
                    } ?: return@res

                    updateByFile2Compress(fileTemp)
                    FileHelper.deleteFileIgnoreException(fileTemp)
                } else if (resCode != RESULT_CANCELED) {
                    toast("未获取到图片")
                }
            }
    }

    private fun imgCrop(file: File) {
        val fileCrop = File(externalCacheDir, "img_bg_crop.jpeg")
        if (!fileCrop.exists()) {
            fileCrop.createNewFile()
        }
        ResultHelper.with(this)
            .startForResult(
                ImgHelper.getCorpIntent(
                    FileHelper.getUri(this, file),
                    fileCrop.toUri()
                )
            )
            .res end@{ isOk2, _, _ ->
                if (isOk2) {
                    updateByFile2Compress(fileCrop)
                }
            }
    }

    private fun updateByFile2Compress(file: File) {
        bgFile = file
        val compressFile =
            ImgHelper.imgCompress(file, File(filesDir, "img_bg_compressed.jpeg")) ?: return
        updateByBitmap(BitmapFactory.decodeFile(compressFile.absolutePath))
    }

    private fun updateDefBg() {
        val defBg = File(filesDir, "def_bg_compressed.jpeg")
        if (defBg.exists()) {
            updateByBitmap(BitmapFactory.decodeFile(defBg.absolutePath))
        } else {
            val res2File =
                ImgHelper.res2File(
                    R.drawable.ic_bg,
                    defBg,
                    quality = 50,
                    opts = BitmapFactory.Options().apply {
                        inSampleSize = 2
                    }) ?: return
            updateByBitmap(BitmapFactory.decodeFile(res2File.absolutePath))
        }
    }

    private fun updateByBitmap(bitmap: Bitmap) {
        vp.addItemDecoration(itemDecoration.setBg(bitmap))
    }

    @Suppress("SameParameterValue")
    private fun adjustBmpRotation(bmp: Bitmap, rotation: Int): Bitmap {
        val m = Matrix()
        m.setRotate(rotation.toFloat(), bmp.width / 2f, bmp.height / 2f)
        val targetX: Float
        val targetY: Float
        if (rotation == 90) {
            targetX = bmp.height.toFloat()
            targetY = 0f
        } else {
            targetX = bmp.height.toFloat()
            targetY = bmp.width.toFloat()
        }

        val values = FloatArray(9)
        m.getValues(values)

        val x1 = values[Matrix.MTRANS_X]
        val y1 = values[Matrix.MTRANS_Y]

        m.postTranslate(targetX - x1, targetY - y1)

        val bm1 = Bitmap.createBitmap(bmp.height, bmp.width, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bm1)
        canvas.drawBitmap(bmp, m, Paint())
        return bm1
    }

    private inner class ViewPagerAdapter(
        activity: FragmentActivity,
        private val count: Int
    ) : FragmentStateAdapter(activity) {

        private val array = arrayOf(
            ImgFragment.newFragment(),
            ImgFragment.newFragment(),
            ImgFragment.newFragment(),
            ImgFragment.newFragment(),
            ImgFragment.newFragment()
        )

        override fun getItemCount() = count

        override fun createFragment(position: Int): Fragment {
            return array[position]
        }

        fun updateFragment(path: Array<String>) {
            array.forEachIndexed { index, imgFragment ->
                imgFragment.updateBg(path[index])
            }
        }
    }

    class ImgFragment : BaseFragment() {

        companion object {

            fun newFragment(): ImgFragment {
                return ImgFragment()
            }
        }

        private lateinit var bg: ViewGroup

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            return inflater.inflate(R.layout.fragment_img, container, false)
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            bg = view.findViewById(R.id.img_fragment_container)
        }

        fun updateBg(path: String) {
            bg.background = Drawable.createFromPath(path)
        }
    }
}