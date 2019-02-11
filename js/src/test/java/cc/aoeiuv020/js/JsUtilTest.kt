package cc.aoeiuv020.js

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Created by AoEiuV020 on 2019.02.11-16:55:05.
 */
class JsUtilTest {

    @Test
    fun run() {
        val start = System.currentTimeMillis()
        assertEquals("hello", JsUtil.run("(function(){return 'hello';}())"))
        // 180, 第一次有初始化环境，
        println(System.currentTimeMillis() - start)
        assertEquals("hello", JsUtil.run("(function(){return 'hello';}())"))
        // 184, 后面没有浪费太多时间，
        println(System.currentTimeMillis() - start)
        assertEquals("hello", JsUtil.run("(function(){return 'hello';}())"))
        println(System.currentTimeMillis() - start)
        assertEquals("hello", JsUtil.run("(function(){return 'hello';}())"))
        println(System.currentTimeMillis() - start)
        assertEquals("hello", JsUtil.run("(function(){return 'hello';}())"))
        println(System.currentTimeMillis() - start)
        assertEquals("hello", JsUtil.run("(function(){return 'hello';}())"))
        println(System.currentTimeMillis() - start)
    }

    @Test
    fun preload() {
        val f = """
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
        """.trimIndent()
        val js = """
            (function(p,a,c,k,e,d){e=function(c){return(c<a?"":e(parseInt(c/a)))+((c=c%a)>35?String.fromCharCode(c+29):c.toString(36))};if(!''.replace(/^/,String)){while(c--)d[e(c)]=k[c]||e(c);k=[function(e){return d[e]}];e=function(){return'\\w+'};c=1;};while(c--)if(k[c])p=p.replace(new RegExp('\\b'+e(c)+'\\b','g'),k[c]);return p;}('j.k({"l":4,"g":"h。i，m。","q":"4.0","r":s,"n":"3","o":["p.0.2","6.0.2","7.0.2","8.0.2","9.0.2","5.0.2","e.0.2","f.0.2","a.0.2","b.0.2","c.0.2","d.0.2","J.0.2","K.0.2","L.0.2","G.0.2","H.0.2","I.0.2"],"P":Q,"M":N,"O":"/F/w/y/3/","v":1,"t":"","u":z,"D":E,"C":{"A":"B"}}).x();',53,53,'FYBw5gPhDuCmBGIKBpvADAFkLvREDMqBsATPhKgaaoRThehQKwUCcpAjKm621WQOwUAOCPAB2AQwC2sCIEQjQPRmgFWzA0c4RAjK6A4uUAxKoGdNQONKgUADAIW6BZ60BNcoFjFQFpagMLkIAZQCyACQgBLCWAAiYgC5jhrgAmEHr6gAhGgFxy1jKGgOd+2hAAxuJSEABmrgA2sADOFFyIrolJQRDorDj0ZMKZAPaJANYA+onFIrAAHj4AksE5fj4ArnkQIABOsN0irj4wORLQAJ4iiwBegRIAjk2Lm65itWWs+PhMHBKBjLWsAFJNUokASvA5j6hOIGIAgg3AYABijy+EBymVGEwAbr0joRYRwQDkeMcILUJGIGrBUFxFkiaFi6FjGNkRBBWEJPj4ABbpVzTHKU2DBNJiTI5WBAA==='['\x73\x70\x6c\x69\x63']('\x7c'),0,{}))
        """.trimIndent()
        val ctx = JsUtil.create()
        ctx.run(f)
        assertEquals("""
            SMH.imgData({"bid":30626,"bname":"我是魔王。由于和女勇者的母亲再婚了，女勇者成为了我的继女。","bpic":"30626.jpg","cid":413500,"cname":"第04话","files":["001.jpg.webp","002.jpg.webp","003.jpg.webp","004.jpg.webp","005.jpg.webp","006.jpg.webp","007.jpg.webp","008.jpg.webp","009.jpg.webp","010.jpg.webp","011.jpg.webp","012.jpg.webp","013.jpg.webp","014.jpg.webp","015.jpg.webp","016.jpg.webp","omake01.jpg.webp","y2.jpg.webp"],"finished":false,"len":18,"path":"/ps2/w/wsmwynyzdmq_yqiao/第04话/","status":1,"block_cc":"","nextId":416690,"prevId":412220,"sl":{"md5":"o1J_mecRbsR0HpaAkjgFRA"}}).preInit();
        """.trimIndent(), ctx.run(js))
    }
}