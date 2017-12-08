package cc.aoeiuv020.pager.test

import android.app.Activity
import android.os.Bundle
import android.widget.FrameLayout
import cc.aoeiuv020.reader.*
import org.jetbrains.anko.ctx
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.util.concurrent.TimeUnit

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val fl = FrameLayout(ctx)
        setContentView(fl)

        val requester = object : TextRequester {
            override fun request(index: Int, refresh: Boolean): Text {
                return Text(List(10) {
                    List(it + 1) {
                        "小说内容" + it
                    }.joinToString(";")
                })
            }
        }
        val config = ReaderConfig(31, 23, 8,
                1, 5, 10, 15,
                0xff000000.toInt(), 0xffffffff.toInt(), null)
        val reader = Readers.getComplexReader(ctx, Novel("书名", "作者名"), fl, requester, config)
        val chapters = List(20) {
            Chapter("章节名" + it)
        }
        reader.chapterList = chapters

        doAsync {
            TimeUnit.SECONDS.sleep(5)
            uiThread {
                reader.currentChapter = 10
            }
        }
    }
}
