package com.munch.project.one.file

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.munch.lib.base.setDoubleClickListener
import com.munch.lib.fast.base.BaseBigTextTitleActivity
import com.munch.lib.fast.base.BaseFragment
import com.munch.lib.fast.recyclerview.SimpleAdapter
import com.munch.lib.fast.recyclerview.setOnItemClickListener
import com.munch.lib.helper.FragmentHelper
import com.munch.lib.helper.getExtension
import com.munch.lib.helper.openIntent
import com.munch.lib.result.with
import com.munch.project.one.R
import com.munch.project.one.databinding.ActivityFileExploreBinding
import com.munch.project.one.databinding.FragmentFileExploreBinding
import com.munch.project.one.databinding.ItemFileExploreBinding
import java.io.File
import java.util.*

/**
 * Create by munch1182 on 2021/11/13 22:37.
 */
class FileExploreActivity : BaseBigTextTitleActivity() {

    private val bind by bind<ActivityFileExploreBinding>()
    private val fg = FragmentHelper(this, R.id.fe_fl_container)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind.feFlContainer
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            with(
                { Environment.isExternalStorageManager() },
                Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
            ).start { showFileExplore(it) }
        } else {
            with(Manifest.permission.READ_EXTERNAL_STORAGE)
                .request { showFileExplore(it) }
        }
    }

    private var hp = false
    private fun showFileExplore(hasPermission: Boolean) {
        val root = if (hasPermission) {
            /*Environment.getExternalStorageDirectory()*/
            File("/storage/emulated/0")
        } else {
            filesDir.parentFile
        } ?: cacheDir
        hp = hasPermission
        showFileExplore(root)
    }

    //todo 回退时没有保留状态
    fun showFileExplore(f: File?) {
        fg.show(FileExploreFragment.newFileExploreFragment(f))
    }

    override fun onBackPressed() {
        if (!fg.pop()) {
            super.onBackPressed()
        }
    }

    fun changeDir() {
        fg.reset()
        showFileExplore(!hp)
    }
}

class FileExploreFragment : BaseFragment() {

    companion object {

        private const val KEY_FILE = "key_file"

        fun newFileExploreFragment(file: File?): FileExploreFragment {
            return FileExploreFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(KEY_FILE, file)
                }
            }
        }
    }

    private val bind by bind<FragmentFileExploreBinding>()
    private val fileAdapter by lazy {
        SimpleAdapter<FEBean, ItemFileExploreBinding>(R.layout.item_file_explore) { _, bind, b ->
            bind.bean = b
        }
    }
    private val activity: FileExploreActivity?
        get() = (getActivity() as? FileExploreActivity)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = bind.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bind.feRv.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = fileAdapter
        }
        //todo 没有设置分段点击，没有滑动悬停
        bind.feLoc.setDoubleClickListener { activity?.changeDir() }
        fileAdapter.setOnItemClickListener { _, pos, _ ->
            val f = fileAdapter.get(pos)?.file ?: return@setOnItemClickListener
            if (f.isDirectory) {
                activity?.showFileExplore(f)
            } else if (f.isFile) {
                f.openIntent()?.let { startActivity(it) }
            }
        }
        val dir = arguments?.getSerializable(KEY_FILE) as? File
        bind.feLoc.text = dir?.absolutePath ?: "..."
        //当文件名有无效utf-8字符时，会有系统的jni错误，无法拦截
        //但是正式版的app没有此报错
        fileAdapter.set(dir?.listFiles()?.map { FEBean(it) }?.sorted()?.toMutableList())
    }
}

data class FEBean(private val f: File) : Comparable<FEBean> {

    companion object {
        private fun File.isOpenable(): Boolean {
            val extension = getExtension() ?: return false
            //todo
            if (extension in arrayOf("txt", "apk", "png", "jpg", "jpeg", "mp3", "mp4", "avi")) {
                return true
            }
            return false
        }
    }

    @BindingAdapter("file_icon")
    fun bind(imageView: ImageView, bean: FEBean) {
        val f = bean.f
        if (f.isFile) {
            if (f.isOpenable()) {
                imageView.setImageResource(R.drawable.ic_file_openable)
            } else {
                imageView.setImageResource(R.drawable.ic_file_unknow)
            }
        } else if (f.isDirectory) {
            val list = f.list()
            if (list.isNullOrEmpty()) {
                imageView.setImageResource(R.drawable.ic_folder_empty)
            } else {
                imageView.setImageResource(R.drawable.ic_folder)
            }
        }
    }

    val name: String
        get() = f.name
    val file: File
        get() = f

    override fun compareTo(other: FEBean): Int {
        if (f.isDirectory && !other.f.isDirectory) {
            return 1
        } else if (!f.isDirectory && other.f.isDirectory) {
            return -1
        }
        return name.compareTo(other.name)
    }

}