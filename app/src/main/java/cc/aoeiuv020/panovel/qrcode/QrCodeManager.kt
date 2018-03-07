package cc.aoeiuv020.panovel.qrcode

import java.net.URLEncoder

/**
 * 二维码相关，
 * Created by AoEiuV020 on 2018.03.07-23:10:39.
 */
object QrCodeManager {
    /**
     * @return 生成二维码图片地址，
     */
    fun generate(str: String): String {
        val data = URLEncoder.encode(str, "UTF-8")
        return "http://tool.oschina.net/action/qrcode/generate?data=$data&error=L&type=0&margin=4&size=4"
    }
}