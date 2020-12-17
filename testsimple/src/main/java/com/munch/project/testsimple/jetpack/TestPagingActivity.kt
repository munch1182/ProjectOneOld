package com.munch.project.testsimple.jetpack

import android.os.Bundle
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.munch.lib.test.recyclerview.TestRvActivity
import com.munch.project.testsimple.R
import com.munch.project.testsimple.jetpack.data.User
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Create by munch1182 on 2020/12/11 17:14.
 */
@AndroidEntryPoint
class TestPagingActivity : TestRvActivity() {

    private val rv by lazy { findViewById<RecyclerView>(R.id.paging_rv) }
    private val viewModel by lazy { ViewModelProvider(this).get(PagingViewModel::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_paging)
        val userAdapter = UserAdapter()
        rv.adapter = userAdapter

        lifecycleScope.launch {
            viewModel.allUser.collectLatest {
                userAdapter.submitData(it)
            }
        }
    }

    class UserAdapter :
        PagingDataAdapter<User, RecyclerView.ViewHolder>(object : DiffUtil.ItemCallback<User>() {
            override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
                return oldItem.name == newItem.name
            }
        }) {
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            (holder.itemView as TextView).text =
                getItem(holder.absoluteAdapterPosition)?.name ?: "null"
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return object : RecyclerView.ViewHolder(TextView(parent.context)) {}
        }
    }
}