package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.base.jar.ImageUtil
import cc.aoeiuv020.base.jar.absHref
import cc.aoeiuv020.base.jar.title
import cc.aoeiuv020.gson.toBean
import cc.aoeiuv020.panovel.api.base.JsNovelContext
import cc.aoeiuv020.panovel.api.firstIntPattern
import cc.aoeiuv020.panovel.api.firstTwoIntPattern
import com.google.gson.annotations.SerializedName

/**
 * Created by AoEiuV020 on 2019.02.11-17:20:32.
 */
class Manhuagui : JsNovelContext() {init {
    // 漫画源默认不开启，
    // 想想还是开启，正好用于搜索小说的漫画版，
    site {
        name = "漫画柜"
        baseUrl = "https://www.manhuagui.com"
        logo = "https://cf.hamreus.com/images/mhg.png"
    }
    search {
        get {
            // https://www.manhuagui.com/s/%E9%AD%94%E7%8E%8B.html
            url = "/s/${utf8(it)}.html"
        }
        document {
            items("div.book-result > ul > li") {
                // 可能有额外的名字在a后面跟个small,
                name("> div.book-detail > dl > dt > a")
                // 可能有多个作者，逗号隔开，
                author = root.requireElements(query = "> div.book-detail > dl > dd:nth-child(4) > span > a", name = TAG_AUTHOR_NAME)
                        .joinToString(",") {
                            it.text()
                        }
            }
        }
    }
    // https://www.manhuagui.com/comic/30626/
    // https://m.manhuagui.com/comic/30626/
    bookIdRegex = firstIntPattern
    detailPageTemplate = "/comic/%s/"
    detail {
        document {
            novel {
                name("div.book-detail.pr.fr > div.book-title > h1")
                author = root.requireElements(query = "div.book-detail.pr.fr > ul > li:nth-child(2) > span:nth-child(2) > a", name = TAG_AUTHOR_NAME)
                        .joinToString(",") {
                            it.text()
                        }
            }
            image("div.book-cover.fl > p > img")
            update("div.book-detail.pr.fr > ul > li.status > span > span:nth-child(3)", format = "yyyy-MM-dd")
            introduction("#intro-all")
        }
    }
    chapters {
        document {
            // 先番外，再单行本，最后单话，
            // https://www.manhuagui.com/comic/17332/
            val list = root.requireElements("div[id^=chapter-list]").flatMap {
                it.requireElements("> ul").reversed()
            }.flatMap {
                it.requireElements("> li > a")
            }.reversed()
            items(list) {
                name = root.title()
                extra = findBookIdWithChapterId(root.absHref())
            }
        }
    }
    // https://www.manhuagui.com/comic/19785/406233.html
    bookIdWithChapterIdRegex = firstTwoIntPattern
    contentPageTemplate = "/comic/%s.html"
    js(JS_SPLIC)
    content {
        document {
            // 图片需要带referer请求的，
            // https://eu.hamreus.com/ps2/w/wsmwynyzdmq_yqiao/%E7%AC%AC04%E8%AF%9D/001.jpg.webp?cid=413500&md5=o1J_mecRbsR0HpaAkjgFRA
            /*
{
  "bid": 30626,
  "bname": "我是魔王。由于和女勇者的母亲再婚了，女勇者成为了我的继女。",
  "bpic": "30626.jpg",
  "cid": 413500,
  "cname": "第04话",
  "files": [
    "001.jpg.webp",
    "002.jpg.webp",
    "003.jpg.webp",
    "004.jpg.webp",
    "005.jpg.webp",
    "006.jpg.webp",
    "007.jpg.webp",
    "008.jpg.webp",
    "009.jpg.webp",
    "010.jpg.webp",
    "011.jpg.webp",
    "012.jpg.webp",
    "013.jpg.webp",
    "014.jpg.webp",
    "015.jpg.webp",
    "016.jpg.webp",
    "omake01.jpg.webp",
    "y2.jpg.webp"
  ],
  "finished": false,
  "len": 18,
  "path": "/ps2/w/wsmwynyzdmq_yqiao/第04话/",
  "status": 1,
  "block_cc": "",
  "nextId": 416690,
  "prevId": 412220,
  "sl": {
    "md5": "o1J_mecRbsR0HpaAkjgFRA"
  }
}

                var r = pVars.manga.filePath + t.files[n] + "?cid=" + t.cid, i;
                for (i in t.sl)
                    r += "&" + i + "=" + t.sl[i];
                return r
             */
            val script = root.script("body > script:nth-child(8)")
                    .removePrefix("""window["\x65\x76\x61\x6c"]""")
            val data: ChapterData = js(script)
                    .removeSurrounding("""SMH.imgData(""", """).preInit();""")
                    .toBean()
            val params = "cid=${data.cid}&" + data.sl.map { (key, value) ->
                "$key=$value"
            }.joinToString("&")
            novelContent = data.files.map { file ->
                ImageUtil.getImageFromUrl(IMG_HOST + data.path + file + "?" + params)
            }
        }
    }
}

    companion object {
        const val IMG_HOST = "https://eu.hamreus.com"
        const val JS_SPLIC = """
var LZString = (function() {
    var f = String.fromCharCode;
    var keyStrBase64 = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=";
    var baseReverseDic = {};
    function getBaseValue(alphabet, character) {
        if (!baseReverseDic[alphabet]) {
            baseReverseDic[alphabet] = {};
            for (var i = 0; i < alphabet.length; i++) {
                baseReverseDic[alphabet][alphabet.charAt(i)] = i
            }
        }
        return baseReverseDic[alphabet][character]
    }
    var LZString = {
        decompressFromBase64: function(input) {
            if (input == null)
                return "";
            if (input == "")
                return null;
            return LZString._0(input.length, 32, function(index) {
                return getBaseValue(keyStrBase64, input.charAt(index))
            })
        },
        _0: function(length, resetValue, getNextValue) {
            var dictionary = [], next, enlargeIn = 4, dictSize = 4, numBits = 3, entry = "", result = [], i, w, bits, resb, maxpower, power, c, data = {
                val: getNextValue(0),
                position: resetValue,
                index: 1
            };
            for (i = 0; i < 3; i += 1) {
                dictionary[i] = i
            }
            bits = 0;
            maxpower = Math.pow(2, 2);
            power = 1;
            while (power != maxpower) {
                resb = data.val & data.position;
                data.position >>= 1;
                if (data.position == 0) {
                    data.position = resetValue;
                    data.val = getNextValue(data.index++)
                }
                bits |= (resb > 0 ? 1 : 0) * power;
                power <<= 1
            }
            switch (next = bits) {
            case 0:
                bits = 0;
                maxpower = Math.pow(2, 8);
                power = 1;
                while (power != maxpower) {
                    resb = data.val & data.position;
                    data.position >>= 1;
                    if (data.position == 0) {
                        data.position = resetValue;
                        data.val = getNextValue(data.index++)
                    }
                    bits |= (resb > 0 ? 1 : 0) * power;
                    power <<= 1
                }
                c = f(bits);
                break;
            case 1:
                bits = 0;
                maxpower = Math.pow(2, 16);
                power = 1;
                while (power != maxpower) {
                    resb = data.val & data.position;
                    data.position >>= 1;
                    if (data.position == 0) {
                        data.position = resetValue;
                        data.val = getNextValue(data.index++)
                    }
                    bits |= (resb > 0 ? 1 : 0) * power;
                    power <<= 1
                }
                c = f(bits);
                break;
            case 2:
                return ""
            }
            dictionary[3] = c;
            w = c;
            result.push(c);
            while (true) {
                if (data.index > length) {
                    return ""
                }
                bits = 0;
                maxpower = Math.pow(2, numBits);
                power = 1;
                while (power != maxpower) {
                    resb = data.val & data.position;
                    data.position >>= 1;
                    if (data.position == 0) {
                        data.position = resetValue;
                        data.val = getNextValue(data.index++)
                    }
                    bits |= (resb > 0 ? 1 : 0) * power;
                    power <<= 1
                }
                switch (c = bits) {
                case 0:
                    bits = 0;
                    maxpower = Math.pow(2, 8);
                    power = 1;
                    while (power != maxpower) {
                        resb = data.val & data.position;
                        data.position >>= 1;
                        if (data.position == 0) {
                            data.position = resetValue;
                            data.val = getNextValue(data.index++)
                        }
                        bits |= (resb > 0 ? 1 : 0) * power;
                        power <<= 1
                    }
                    dictionary[dictSize++] = f(bits);
                    c = dictSize - 1;
                    enlargeIn--;
                    break;
                case 1:
                    bits = 0;
                    maxpower = Math.pow(2, 16);
                    power = 1;
                    while (power != maxpower) {
                        resb = data.val & data.position;
                        data.position >>= 1;
                        if (data.position == 0) {
                            data.position = resetValue;
                            data.val = getNextValue(data.index++)
                        }
                        bits |= (resb > 0 ? 1 : 0) * power;
                        power <<= 1
                    }
                    dictionary[dictSize++] = f(bits);
                    c = dictSize - 1;
                    enlargeIn--;
                    break;
                case 2:
                    return result.join('')
                }
                if (enlargeIn == 0) {
                    enlargeIn = Math.pow(2, numBits);
                    numBits++
                }
                if (dictionary[c]) {
                    entry = dictionary[c]
                } else {
                    if (c === dictSize) {
                        entry = w + w.charAt(0)
                    } else {
                        return null
                    }
                }
                result.push(entry);
                dictionary[dictSize++] = w + entry.charAt(0);
                enlargeIn--;
                w = entry;
                if (enlargeIn == 0) {
                    enlargeIn = Math.pow(2, numBits);
                    numBits++
                }
            }
        }
    };
    return LZString
}
)();
String.prototype.splic = function(f) {
    return LZString.decompressFromBase64(this).split(f)
}
;
    """
    }

    data class ChapterData(
            @SerializedName("bid")
            val bid: Int,
            @SerializedName("block_cc")
            val blockCc: String,
            @SerializedName("bname")
            val bname: String,
            @SerializedName("bpic")
            val bpic: String,
            @SerializedName("cid")
            val cid: Int,
            @SerializedName("cname")
            val cname: String,
            @SerializedName("files")
            val files: List<String>,
            @SerializedName("finished")
            val finished: Boolean,
            @SerializedName("len")
            val len: Int,
            @SerializedName("nextId")
            val nextId: Int,
            @SerializedName("path")
            val path: String,
            @SerializedName("prevId")
            val prevId: Int,
            @SerializedName("sl")
            val sl: Map<String, String>,
            @SerializedName("status")
            val status: Int
    )
}

