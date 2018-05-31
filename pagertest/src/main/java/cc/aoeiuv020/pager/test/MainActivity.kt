package cc.aoeiuv020.pager.test

import android.app.Activity
import android.os.Bundle
import org.jetbrains.anko.AnkoLogger

class MainActivity : Activity(), AnkoLogger {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
/*

        val fl = FrameLayout(ctx)
        setContentView(fl)

        val requester = object : TextRequester {
            override fun request(index: Int, refresh: Boolean): List<String> {
                return List(10) {
                    List(it + 1) {
                        "小说内容ablIj" + it
                    }.joinToString(";")
                }
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (PackageManager.PERMISSION_GRANTED != checkCallingOrSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 0)
                return
            }
        }

        val config = ReaderConfig(31, 11, 8,
                Margins(), Margins(), Margins(), Margins(), Margins(), Margins(),
                12, "HH:mm:ss",
                0xff000000.toInt(), 0xffffffff.toInt(), null,
                AnimationMode.SIMULATION, animationSpeed = 0.2f)
        val reader = Readers.getReader(ctx, Novel("书名", "作者名"), fl, requester, config)
        val chapters = List(20) {
            "章节名$it"
        }
        reader.chapterList = chapters
*/
    }
}
