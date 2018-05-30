package cc.aoeiuv020.panovel.api

import cc.aoeiuv020.base.jar.gsonJsonPathInit
import cc.aoeiuv020.base.jar.typeRef
import com.google.gson.JsonObject
import com.jayway.jsonpath.JsonPath
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Created by AoEiuV020 on 2018.05.30-11:59:43.
 */
class JsonPathTest {
    init {
        gsonJsonPathInit()
    }

    @Test
    fun path() {
        val json = """
[
  {
    "item": {
      "author": "圣骑士的传说",
      "name": "修真聊天群",
      "requester": {
        "extra": "https://book.qidian.com/info/3602691"
      },
      "site": "起点中文"
    },
    "progress": {
      "chapter": 1950,
      "text": 0
    }
  },
  {
    "item": {
      "author": "奶牛不说话",
      "name": "最强修真在都市",
      "requester": {
        "extra": "http://www.76wx.com/book/5017/"
      },
      "site": "齐鲁文学"
    },
    "progress": {
      "chapter": 0,
      "text": 0
    }
  }
]
"""
        val list: List<JsonObject> = JsonPath.parse(json).read("@", typeRef())
        val nt = list.map {
            JsonPath.parse(it).run {
                val name: String = read("$.item.name", typeRef())
                val progress: Int = read("$.progress.chapter", typeRef())
                name to progress
            }
        }
        assertEquals("修真聊天群" to 1950, nt.first())
        assertEquals("最强修真在都市" to 0, nt.last())

    }
}
