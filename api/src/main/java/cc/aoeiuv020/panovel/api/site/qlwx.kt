package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.base.jar.hex
import cc.aoeiuv020.panovel.api.base.DslJsoupNovelContext
import okhttp3.Cookie
import java.net.URL

/**
 * Created by AoEiuV020 on 2018.05.10-18:11:57.
 */
class Qlwx : DslJsoupNovelContext() { init {
    // 时不时开云锁，不稳定，
    enabled = false
    site {
        name = "齐鲁文学"
        baseUrl = "http://www.76wx.com"
        logo = "http://www.76wx.com/images/book_logo.png"
    }
    search {
        get {
            charset = "GBK"
            url = "/modules/article/search.php"
            data {
                "searchtype" to "articlename"
                "searchkey" to it
                // 加上&page=1可以避开搜索时间间隔的限制，
                // 也可以通过不加载cookies避开搜索时间间隔的限制，
                "page" to "1"
            }
        }
        document {
            if (URL(root.ownerDocument().location()).path.startsWith("/book/")) {
                single {
                    val eInfo = element(query = "#maininfo #info")
                    name("> h1", eInfo)
                    author("> p:nth-child(2)", eInfo, block = pickString("作\\s*者：(\\S*)"))
                }
            } else {
                items("#main > table > tbody > tr:not(:nth-child(1))") {
                    name("td:nth-child(1) > a")
                    author("td:nth-child(3)")
                }
            }
        }
    }
    detailPageTemplate = "/book/%s/"
    detail {
        document {
            val eInfo = element(query = "#maininfo #info")
            novel {
                name("> h1", eInfo)
                author("> p:nth-child(2)", eInfo, block = pickString("作\\s*者：(\\S*)"))
            }
            image("#fmimg > img")
            introduction("#intro > p:not(:nth-last-child(1))")
            update("> p:nth-child(4)", parent = eInfo, format = "yyyy-MM-dd HH:mm:ss", block = pickString("更新时间：(.*)"))
        }
    }
    chapters {
        // 开头 9 章可能是网站上显示的最新章节，和列表最后重复，
        // 但也可能没有这重复的 9 章，
        val list = document {
            items("#list > dl > dd > a")
            lastUpdate("#maininfo #info > p:nth-child(4)", format = "yyyy-MM-dd HH:mm:ss", block = pickString("更新时间：(.*)"))
        }
        var index = 0
        // 以防万一，
        if (list.size == 1) return@chapters list
        // 倒序列表判断是否重复章节，
        // 最后一章被填充了更新时间，第一章重复的没有，所以不能直接==判断NovelChapter对象，
        val reversedList = list.asReversed()
        list.dropWhile {
            (it.extra == reversedList[index].extra).also { ++index }
        }
    }
    // http://www.76wx.com/book/161/892418.html
    contentPageTemplate = "/book/%s.html"
    content {
        document {
            items("#content")
        }
    }
    /*
          function stringToHex(str) {
              var val = "";
              for (var i = 0; i < str.length; i++) {
                  if (val == "") val = str.charCodeAt(i).toString(16);
                  else val += str.charCodeAt(i).toString(16);
              }
              return val;
          }

          function YunSuoAutoJump() {
              var width = screen.width;
              var height = screen.height;
              var screendate = width + "," + height;
              var curlocation = window.location.href;
              if (-1 == curlocation.indexOf("security_verify_")) {
                  document.cookie = "srcurl=" + stringToHex(window.location.href) + ";path=/;";
              }
              self.location = "/modules/article/reader.php?aid=454&security_verify_data=" + stringToHex(screendate);
          }
   */
    // 这网站用了云锁，
    // 验证失败就会跳到验证页面，给个cookie, security_session_mid_verify=dc8d646a63a704e5a11228276a0c385d
    // 大概三天超时，
    // 然后这个cookie要请求两次才能拿到，第一次没有拿到cookie会再跳一次，
    interceptor {
        val request = it.request()
        val response = it.proceed(request)
        val cookies = Cookie.parseAll(response.request().url(), response.headers())
        // 可能不准，第一次跳到验证页时肯定是request里没有这个cookie, 相应的response里一定有这个cookie,
        // 校验超时就不好说了，okhttp在cookie超时时不会带上，有的万一的话应该是cookie被影响了，超时信息不见了，
        // 判断responseBody的话影响就比较大了，
        // 判断security_session_mid_verify的话可能这网站什么时候没云锁就糟了，
        if (cookies.any { it.name() == "yunsuo_session_verify" }) {
            val w = 1080
            val h = 1920
            val d = "$w,$h"
            response(connect(absUrl("/modules/?&security_verify_data=${d.hex()}")))
            // 验证完了重新请求，
            // 搞不好死循环了就糟糕了，
            response(client.newCall(response.request()))
        } else {
            response
        }
    }
}


}

