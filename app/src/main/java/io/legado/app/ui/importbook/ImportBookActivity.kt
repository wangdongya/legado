package io.legado.app.ui.importbook

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import io.legado.app.App
import io.legado.app.R
import io.legado.app.base.VMBaseActivity
import io.legado.app.help.AppConfig
import io.legado.app.lib.theme.accentColor
import io.legado.app.utils.DocumentUtils
import io.legado.app.utils.getViewModel
import kotlinx.android.synthetic.main.activity_import_book.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.anko.sdk27.listeners.onClick
import java.io.File


class ImportBookActivity : VMBaseActivity<ImportBookViewModel>(R.layout.activity_import_book),
    ImportBookAdapter.CallBack {
    private val requestCodeSelectFolder = 342
    private var rootDoc: DocumentFile? = null
    private val subDocs = arrayListOf<DocumentFile>()
    private lateinit var adapter: ImportBookAdapter
    private var localUriLiveData: LiveData<List<String>>? = null

    override val viewModel: ImportBookViewModel
        get() = getViewModel(ImportBookViewModel::class.java)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        initView()
        initEvent()
        initData()
        upRootDoc()
    }

    override fun onCompatCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.import_book, menu)
        return super.onCompatCreateOptionsMenu(menu)
    }

    override fun onCompatOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_select_folder -> selectImportFolder()
        }
        return super.onCompatOptionsItemSelected(item)
    }

    private fun initView() {
        recycler_view.layoutManager = LinearLayoutManager(this)
        adapter = ImportBookAdapter(this, this)
        recycler_view.adapter = adapter
        rotate_loading.loadingColor = accentColor
    }

    private fun initEvent() {
        tv_go_back.onClick {
            goBackDir()
        }
        btn_add_book.onClick {
            viewModel.addToBookshelf(adapter.selectedUris) {
                upPath()
            }
        }
        btn_delete.onClick {
            viewModel.deleteDoc(adapter.selectedUris) {
                upPath()
            }
        }
        cb_selected_all.onClick {
            adapter.selectAll(cb_selected_all.isChecked)
        }
    }

    private fun initData() {
        localUriLiveData?.removeObservers(this)
        localUriLiveData = App.db.bookDao().observeLocalUri()
        localUriLiveData?.observe(this, Observer {
            adapter.upBookHas(it)
        })
    }

    private fun upRootDoc() {
        AppConfig.importBookPath?.let {
            val rootUri = Uri.parse(it)
            rootDoc = DocumentFile.fromTreeUri(this, rootUri)
            subDocs.clear()
            upPath()
        }
    }

    @SuppressLint("SetTextI18n")
    @Synchronized
    private fun upPath() {
        rootDoc?.let { rootDoc ->
            var path = rootDoc.name.toString() + File.separator
            var lastDoc = rootDoc
            for (doc in subDocs) {
                lastDoc = doc
                path = path + doc.name + File.separator
            }
            tv_path.text = path
            adapter.selectedUris.clear()
            adapter.clearItems()
            rotate_loading.show()
            launch(IO) {
                val docList = DocumentUtils.listFiles(
                    this@ImportBookActivity,
                    lastDoc.uri
                )
                for (i in docList.lastIndex downTo 0) {
                    val item = docList[i]
                    if (item.name.startsWith(".")) {
                        docList.removeAt(i)
                    } else if (!item.isDir && !item.name.endsWith(".txt", true)) {
                        docList.removeAt(i)
                    }
                }
                docList.sortWith(compareBy({ !it.isDir }, { it.name }))
                withContext(Main) {
                    rotate_loading.hide()
                    adapter.setData(docList)
                }
            }
        }
    }

    private fun selectImportFolder() {
        try {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            startActivityForResult(intent, requestCodeSelectFolder)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            requestCodeSelectFolder -> if (resultCode == Activity.RESULT_OK) {
                data?.data?.let {
                    contentResolver.takePersistableUriPermission(
                        it,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    )
                    AppConfig.importBookPath = it.toString()
                    upRootDoc()
                }
            }
        }
    }

    @Synchronized
    override fun nextDoc(doc: DocumentFile) {
        subDocs.add(doc)
        upPath()
    }

    @Synchronized
    private fun goBackDir(): Boolean {
        return if (subDocs.isNotEmpty()) {
            subDocs.removeAt(subDocs.lastIndex)
            upPath()
            true
        } else {
            false
        }
    }

    override fun onBackPressed() {
        if (!goBackDir()) {
            super.onBackPressed()
        }
    }

    override fun upCountView() {
        if (adapter.checkableCount == 0) {
            cb_selected_all.isChecked = false
        } else {
            cb_selected_all.isChecked = adapter.selectedUris.size >= adapter.checkableCount
        }

        //重置全选的文字
        if (cb_selected_all.isChecked) {
            cb_selected_all.text = getString(
                R.string.select_cancel_count,
                adapter.selectedUris.size,
                adapter.checkableCount
            )
        } else {
            cb_selected_all.text = getString(
                R.string.select_all_count,
                adapter.selectedUris.size,
                adapter.checkableCount
            )
        }
        setMenuClickable(adapter.selectedUris.isNotEmpty())
    }

    private fun setMenuClickable(isClickable: Boolean) {
        //设置是否可删除
        btn_delete.isEnabled = isClickable
        btn_delete.isClickable = isClickable
        //设置是否可添加书籍
        btn_add_book.isEnabled = isClickable
        btn_add_book.isClickable = isClickable
    }

}