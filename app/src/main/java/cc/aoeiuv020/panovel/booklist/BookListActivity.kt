package cc.aoeiuv020.panovel.booklist

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import cc.aoeiuv020.panovel.api.NovelItem
import cc.aoeiuv020.panovel.local.BookListData
import cc.aoeiuv020.panovel.local.toBean
import cc.aoeiuv020.panovel.local.toJson
import cc.aoeiuv020.panovel.text.NovelTextActivity
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import org.jetbrains.anko.selector
import org.jetbrains.anko.startActivity

/**
 *
 * Created by AoEiuV020 on 2017.11.22-14:49:22.
 */
class BookListActivity : AppCompatActivity(), AnkoLogger {
    companion object {
        fun start(context: Context, bookListData: BookListData) {
            context.startActivity<BookListActivity>("bookListData" to bookListData.toJson())
        }
    }

    private lateinit var bookListData: BookListData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        info {
            "onCreate"
        }

        bookListData = intent.getStringExtra("bookListData").toBean()

        info {
            bookListData
        }

        selector(bookListData.name, bookListData.list.map(NovelItem::name)) { _, i ->
            val novelItem = bookListData.list[i]
            NovelTextActivity.start(this, novelItem)
        }
    }
}