package cc.aoeiuv020.panovel.api

import com.google.gson.GsonBuilder
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 *
 * Created by AoEiuV020 on 2017.10.08-17:18:30.
 */
class NovelItemTest {
    private val str = """{"site":"飘天文学","name":"元尊","author":"天蚕土豆","requester":{"type":"cc.aoeiuv020.panovel.api.DetailRequester","extra":"http://www.piaotian.com/bookinfo/8/8955.html"}}"""
    private val item = NovelItem("飘天文学", "元尊", "天蚕土豆", "http://www.piaotian.com/bookinfo/8/8955.html")
    private val gson = GsonBuilder().paNovel().create()
    @Test
    fun gson() {
        gson.toJson(item).let {
            assertEquals(str, it)
        }
        gson.fromJson(str, NovelItem::class.java).let {
            assertEquals(item, it)
        }
    }
}

