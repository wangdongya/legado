package io.legado.app.ui.book.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.RecyclerView
import io.legado.app.R
import io.legado.app.data.entities.SearchShow
import io.legado.app.help.ImageLoader
import io.legado.app.utils.gone
import io.legado.app.utils.visible
import kotlinx.android.synthetic.main.item_bookshelf_list.view.iv_cover
import kotlinx.android.synthetic.main.item_bookshelf_list.view.tv_name
import kotlinx.android.synthetic.main.item_search.view.*
import org.jetbrains.anko.sdk27.listeners.onClick

class SearchAdapter(val callBack: CallBack) :
    PagedListAdapter<SearchShow, SearchAdapter.MyViewHolder>(DiffCallBack()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_search, parent, false))
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
        } else {
            getItem(position)?.let {
                holder.bindChange(it, payloads)
            }
        }
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        getItem(position)?.let {
            holder.bind(it, callBack)
        }
    }


    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun bind(searchBook: SearchShow, callBack: CallBack?) = with(itemView) {
            tv_name.text = searchBook.name
            tv_author.text = context.getString(R.string.author_show, searchBook.author)
            bv_originCount.setBadgeCount(searchBook.originCount)
            if (searchBook.latestChapterTitle.isNullOrEmpty()) {
                tv_lasted.gone()
            } else {
                tv_lasted.text = context.getString(R.string.lasted_show, searchBook.latestChapterTitle)
                tv_lasted.visible()
            }
            tv_introduce.text = context.getString(R.string.intro_show, searchBook.intro)
            val kinds = searchBook.getKindList()
            if (kinds.isEmpty()) {
                ll_kind.gone()
            } else {
                ll_kind.visible()
                for (index in 0..2) {
                    if (kinds.size > index) {
                        when (index) {
                            0 -> {
                                tv_kind.text = kinds[index]
                                tv_kind.visible()
                            }
                            1 -> {
                                tv_kind_1.text = kinds[index]
                                tv_kind_1.visible()
                            }
                            2 -> {
                                tv_kind_2.text = kinds[index]
                                tv_kind_2.visible()
                            }
                        }
                    } else {
                        when (index) {
                            0 -> tv_kind.gone()
                            1 -> tv_kind_1.gone()
                            2 -> tv_kind_2.gone()
                        }
                    }
                }
            }
            searchBook.coverUrl.let {
                ImageLoader.load(context, it)//Glide自动识别http://和file://
                    .placeholder(R.drawable.img_cover_default)
                    .error(R.drawable.img_cover_default)
                    .centerCrop()
                    .setAsDrawable(iv_cover)
            }
            onClick {
                callBack?.showBookInfo(searchBook.name, searchBook.author)
            }
        }

        fun bindChange(searchBook: SearchShow, payloads: MutableList<Any>) =
            with(itemView) {
                when (payloads[0]) {
                    1 -> bv_originCount.setBadgeCount(searchBook.originCount)
                    2 -> searchBook.coverUrl.let {
                        ImageLoader.load(context, it)//Glide自动识别http://和file://
                            .placeholder(R.drawable.img_cover_default)
                            .error(R.drawable.img_cover_default)
                            .centerCrop()
                            .setAsDrawable(iv_cover)
                    }
                    3 -> {
                        val kinds = searchBook.getKindList()
                        if (kinds.isEmpty()) {
                            ll_kind.gone()
                        } else {
                            ll_kind.visible()
                            for (index in 0..2) {
                                if (kinds.size > index) {
                                    when (index) {
                                        0 -> {
                                            tv_kind.text = kinds[index]
                                            tv_kind.visible()
                                        }
                                        1 -> {
                                            tv_kind_1.text = kinds[index]
                                            tv_kind_1.visible()
                                        }
                                        2 -> {
                                            tv_kind_2.text = kinds[index]
                                            tv_kind_2.visible()
                                        }
                                    }
                                } else {
                                    when (index) {
                                        0 -> tv_kind.gone()
                                        1 -> tv_kind_1.gone()
                                        2 -> tv_kind_2.gone()
                                    }
                                }
                            }
                        }
                    }
                    4 -> {
                        if (searchBook.latestChapterTitle.isNullOrEmpty()) {
                            tv_lasted.gone()
                        } else {
                            tv_lasted.text = context.getString(
                                R.string.lasted_show,
                                searchBook.latestChapterTitle
                            )
                            tv_lasted.visible()
                        }
                    }
                    5 -> tv_introduce.text =
                        context.getString(R.string.intro_show, searchBook.intro)
                }
            }
    }

    interface CallBack {
        fun showBookInfo(name: String, author: String)
    }
}