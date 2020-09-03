import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.munch.project.one.R
import com.munch.project.one.base.BaseActivity
import kotlinx.android.synthetic.main.activity_rv.*

/**
 * Create by munch on 2020/9/2 9:41
 */
open class RvActivity : BaseActivity() {

    open lateinit var adapter: BaseQuickAdapter<String, BaseViewHolder>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rv)

        setSupportActionBar(rv_tb)
        supportActionBar!!.elevation = 15f

        rv_rv.layoutManager = LinearLayoutManager(this)
        val itemList = ArrayList<String>().apply {
            addItemList(this)
        }
        adapter =
            object : BaseQuickAdapter<String, BaseViewHolder>(R.layout.item_rv_main, itemList) {
                override fun convert(holder: BaseViewHolder, item: String) {
                    holder.setText(R.id.item_tv, item)
                }
            }
        rv_rv.adapter = adapter
    }

    open fun addItemList(list: ArrayList<String>) {
    }
}