package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.encrypt.*
import org.junit.Assert.assertEquals
import org.junit.Test
import java.nio.ByteBuffer
import java.nio.charset.Charset

/**
 * Created by AoEiuV020 on 2018.09.04-03:47:10.
 */
class LnovelTest : BaseNovelContextText(Lnovel::class) {
    @Test
    fun search() {
        search("异世界")
        search("都市")
        search("OVERLORD不死者之王", "丸山くがね", "1592")
        search("不正经的魔术讲师与禁忌教典", "羊太郎", "1829")
        // 全名太长，搜索失败，
//        search("为美好的世界献上祝福！ / 给予这个绝美的世界以祝福！", "晓なつめ", "1657")
    }

    @Test
    fun detail() {
        detail("1592", "1592",
                "OVERLORD不死者之王", "丸山くがね",
                "https://lnovel.cc/cover/1592s.jpg",
                "一款席卷游戏界的网路游戏「YGGDRASIL」，有一天突然毫无预警地停止一切服务——原本应该是如此。但是不知为何它却成了一款即使过了结束时间，玩家角色依然不会登出的游戏。NPC开始拥有自己的思想。\n" +
                        "现实世界当中一名喜欢电玩的普通青年，似乎和整个公会一起穿越到异世界，变成拥有骷髅外表的最强魔法师「飞鼠」。他率领的公会「安兹．乌尔．恭」将展开前所未有的奇幻传说！",
                "2018-05-23 00:00:00")
        detail("1657", "1657",
                "为美好的世界献上祝福！ / 给予这个绝美的世界以祝福！", "晓なつめ",
                "https://lnovel.cc/cover/1657s.jpg",
                "喜爱游戏的家里蹲少年佐藤和真的人生突然闭幕……但是他的眼前出现自称女神的美少女。转生到异世界的和真就此为了满足食衣住而努力工作！原本只想安稳度日的和真，却因为带去的女神接二连三引发问题，甚至被魔王军盯上了！？",
                "2018-05-31 00:00:00")
    }

    @Test
    fun chapters() {
        chapters("1657",
                "人物简介", "56212", null,
                "g店特典 假面店主的掏耳膝枕", "86695", "2018-05-31 00:00:00",
                238)
        chapters("1592",
                "prologue", "53863", null,
                "插图", "89626", "2018-05-23 00:00:00",
                119)
    }

    @Test
    fun content() {
        content("56212",
                "台版 转自 轻之国度",
                "和真：……我的小队哪有这么令人「遗憾」。\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000",
                49)
        content("61675",
                "![img](http://pic.wkcdn.com/pictures/1/1592/61675/75842.jpg)",
                "![img](http://pic.wkcdn.com/pictures/1/1592/61675/75855.jpg)",
                14)
    }


    @Test
    fun decryptTest() {
        // html底部的script反混淆eval后得到下面加密js,
        /*
            axios['get']('/content/' + info['id'] + '.json')['then'](function(a) {
                var b = CryptoJS['enc']['Hex']['parse'](CryptoJS.MD5(' . ' + navigator['userAgent']).toString()),
                    _0x1581x3 = {
                        iv: CryptoJS['enc']['Hex']['parse'](CryptoJS.SHA1(navigator['userAgent'] + 'novel').toString()['substr'](4, 32)),
                        padding: CryptoJS['pad']['ZeroPadding']
                    };
                b = CryptoJS['AES']['decrypt'](a['data']['content'], b, _0x1581x3).toString(CryptoJS['enc'].Utf8);
                a['data']['content'] = b;
                app['info'] = a['data']
            })['catch'](function(a) {
                console['log'](a);
                alert('\u5C0F\u8BF4\u5185\u5BB9\u83B7\u53D6\u5931\u8D25\uFF01\u8BF7\u7A0D\u540E\u5237\u65B0\u91CD\u8BD5');
                app['info']['title'] = '\u5C0F\u8BF4\u5185\u5BB9\u83B7\u53D6\u5931\u8D25\uFF01\u8BF7\u7A0D\u540E\u5237\u65B0\u91CD\u8BD5'
            })
         */
        /*
            显然，密钥是userAgent加上固定后缀'novel‘再sha1加密后截取中间32位转成的四个数字，
            aes加密的padding是ZeroPadding用零补齐，java不支持，这就有点麻烦了，
            mode是默认，默认是CBC，
            来源，https://code.google.com/archive/p/crypto-js/，其中标题，Block Modes and Padding，
            最后data.content是base64加密过的，在java这里要先解base64,
         */
        val userAgent = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/68.0.3440.106 Safari/537.36"
        val key = " . $userAgent".md5()
        assertEquals("ed53e0029ba9e3b53e5574b931dc2fe8", key.hex())
        val keyIntList = ByteBuffer.wrap(key).asIntBuffer().run {
            List(limit()) {
                get(it)
            }
        }
        assertEquals(listOf(
                -313270270,
                -1683364939,
                1045787833,
                836513768
        ), keyIntList)
        val iv = (userAgent + "novel").sha1().run {
            // hex().drop(4).take(32)
            ByteArray(16) {
                get(it + 2)
            }
        }
        assertEquals("1296c97aa7cf9948145bc75fd1f918b1", iv.hex())
        val ivIntList = ByteBuffer.wrap(iv).asIntBuffer().run {
            List(limit()) {
                get(it)
            }
        }
        assertEquals(listOf(
                311871866,
                -1479567032,
                341559135,
                -772204367
        ), ivIntList)
        val content = """uxyM0LlCYMN5hNsN6jooxEqC9pd5EBvZl+/ypp64psmHtGNWt2mej488FlOb6bCsdQdklNudofpQbphSoWSYBL9oL502CNixt4qIAgFBTGPjsv+P7mEGbBXeuTiaEpwpAtcVpdXYSMTPaXDi4lIW2MdoEG8R9uFMu4DYfQKGiysz80eJjPZGarwcZxtKDPjbD8+9hXlKK34EoWhKd+SUHvVkDPx1+mBSnzIjRJNu0/oO7OG/q1UIJeDAvsnOJr9n5sntFfhy/Q/u9DJ59Ce8g4cKdOd9ggEya0NEjk0lBk213ggIk6g1VuT9J9UsLzoS44eTy2EnWNpOSnFBAtzRolVZNXj38PsKGhOHguArB1tkdTV4Ba/ZoZWSOUTjDvx1Lvvkw6rlYQMpQtVgzCcVb1kuFEFQPreLEAD+RuLAmLmlkm7dg5bImaj3N/qSbAJjKjtl8mZc8XD/W7uMD59opC/XHffxrYQc/tpAqvRk7lnvx8QanIjUWBmJ/uXfnMANJM2JRolcryXo47cUroYjaLTsH5S0dE/jAeTWrWSenHQk5fV+VOS2J8k53ggAiTTUMBzwBfggIGdkc725RXeFWX6DsjJ0L+uQic4p2hKs7+H3qujzMuf3573BeCesdkiT+86qqQB6hcKM395r6eOkRcygtIu1O+xI6Qzg83A/Yl7UEoru9a9pXd70AhmCerR/FW5UcaNkd+0cvzr51vvL6HRiaCjbKoIUPccFALloKLm8cW3y/tyfSSNA+TLgB1tAS8/xLb9aP8P0nXP6hCcov2voi6f5nrr+xL1XqAuXvWNpkjz0FVOwz5lNB25bjordzOKUQ1k0QOICt4AOoAfjrpc3Y9rtTfHld8dgBNgVekVis39jyGICKTow8wRpnGes9JCj6XQbGTuszHuG8Dipu9+Xv1yrOSBUrSKXy1pbeSuA5N23qDKyQ1GPSnWl1S9cWG/vGyIcaVn2ZPQbhgJR4gRxw1I1U2Mo33riklTTjBRFusErvdumtZkJgtVHHSXDLt0ecKRY/PoWQhOurStAv/yHCi60vqTJEa+E9BLOW13Ml9gmnQmspGODvLPUN/bZnb181ZjIovO91RyOfTU4lk1kUs/H2yOUwOupSOYy4iBDRz5IRJ+s4QPdFLouQwpcA4kCHDzldONo30Y4nB7w/GVAPYFcDlJ6MGLU55L5ks9Z+CjZH9jt/CLypisydW8gRENR30fihJ8xGaAYB8gwcG9CJyucgwBZou9MiIyQUbqnbe16plmIfTt4Xa0l9b1dzM0TZ1JmVvQQa7zqpwfbbF+KwHST9WmypV3LBUEfolkB0FcK+4f9UAQq1U9mxnGuP9tOMktJhYfeKjIanfsJCsnaLqf1MW0vNVt3Xn0c8OznT0xYyKVR929Iwj2Mc3XCMmq8yy0v9ZZpae/bCbR0XZyLo0LST2eQPUMcHLPLvl4bvjiaF92dnsDthLSRUMlfwA8CF3StoxThwTDOiO8hYg=="""
                .base64Decode()
                .let {
                    // 手动ZeroPadding, 补齐16字节，
                    // 好像会错意了，补零是加密前补的，但不要紧，这里应该是必然是16的倍数，
                    if (it.size % 16 != 0) {
                        val newSize = (it.size / 16 + 1) * 16
                        it.copyOf(newSize)
                    } else {
                        it
                    }
                }
        // 解密后有多余的0, 不好搞定，也没关系，应该是加密时的zeroPadding补的0,
        val plant = content.cipherDecrypt(key, iv, "AES", "CBC", "NoPadding")
                .toString(Charset.defaultCharset())
        assertEquals("" +
                "&nbsp;&nbsp;&nbsp;&nbsp;网译版 转自 百度贴吧<br />  <br />" +
                "  &nbsp;&nbsp;&nbsp;&nbsp;扫图：WolffyL<br />  <br />" +
                "  &nbsp;&nbsp;&nbsp;&nbsp;翻译：WolffyL<br />  <br />" +
                "  &nbsp;&nbsp;&nbsp;&nbsp;在那夏天的那日。<br />  <br />" +
                "  &nbsp;&nbsp;&nbsp;&nbsp;我回到战场了。<br />  <br />" +
                "  &nbsp;&nbsp;&nbsp;&nbsp;不，是被回到战场。<br />  <br />" +
                "  &nbsp;&nbsp;&nbsp;&nbsp;然后我就很清楚一件事。<br />  <br />" +
                "  &nbsp;&nbsp;&nbsp;&nbsp;人生最重要的地方，并不是战场。<br />  <br />" +
                "  &nbsp;&nbsp;&nbsp;&nbsp;而是家人等待著自己的日常。<br />  <br />" +
                "  &nbsp;&nbsp;&nbsp;&nbsp;人生最重要的行为，并不是赌博。<br />  <br />" +
                "  &nbsp;&nbsp;&nbsp;&nbsp;而是著实活好每一天。<br />  <br />" +
                "  &nbsp;&nbsp;&nbsp;&nbsp;而一直没察觉到这一点的我，<br />  <br />" +
                "  &nbsp;&nbsp;&nbsp;&nbsp;那天，在战场死掉了。<br />  <br />" +
                "  &nbsp;&nbsp;&nbsp;&nbsp;那日──<br />  <br />" +
                "  &nbsp;&nbsp;&nbsp;&nbsp;我回到了日常的生活去了。<br />  <br />" +
                "  &nbsp;&nbsp;&nbsp;&nbsp;我永远忘不了，那夏天的那日的事。" +
                "\u0000\u0000\u0000\u0000\u0000\u0000",
                plant)

    }
}