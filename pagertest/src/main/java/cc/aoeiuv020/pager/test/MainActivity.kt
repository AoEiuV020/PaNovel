package cc.aoeiuv020.pager.test

import android.app.Activity
import android.os.Bundle
import android.widget.FrameLayout
import cc.aoeiuv020.reader.*
import cc.aoeiuv020.reader.complex.ComplexConfig
import org.jetbrains.anko.ctx

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val fl = FrameLayout(ctx)
        setContentView(fl)

        val requester = object : TextRequester {
            override fun request(index: Int, refresh: Boolean): Text {
                return Text(List(40) {
                    List(it) {
                        "小说内容" + it
                    }.joinToString(";")
                })
            }
        }
        val config = ComplexConfig(31, 23, 8,
                1, 5, 10, 15,
                0xff000000.toInt(), 0xffffffff.toInt(), null)
        val reader = Readers.getComplexReader(ctx, Novel("书名", "作者名"), fl, requester, config)
        val chapters = List(10) {
            Chapter("章节名" + it)
        }
        reader.chapterList = chapters
    }
}
