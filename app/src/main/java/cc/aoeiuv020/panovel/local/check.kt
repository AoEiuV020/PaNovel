package cc.aoeiuv020.panovel.local

import android.content.Context
import cc.aoeiuv020.panovel.check.SignatureChecker
import cc.aoeiuv020.panovel.util.async
import io.reactivex.Observable
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.alert
import org.jetbrains.anko.browse

/**
 *
 * Created by AoEiuV020 on 2018.03.25-04:00:29.
 */
object Check : BaseLocalSource(), AnkoLogger {
    private const val AOEIUV020_SIGNATURE = "F473239FE5E994CC7FF64F505D0F0BB6F8E3CB8C"
    private const val RELEASE_COOLAPK = "https://www.coolapk.com/apk/167994"
    private const val RELEASE_GITHUB = "https://github.com/AoEiuV020/PaNovel/releases"
    private var ignoreSignatureCheck: Boolean by PrimitiveDelegate(false)
    private var signature: String? by NullablePrimitiveDelegate()

    /**
     * @return 忽略或者通过都返回true,
     */
    private fun checkSignature(ctx: Context): Boolean {
        if (ignoreSignatureCheck) {
            return true
        }
        val apkSign = signature
                ?: SignatureChecker.validateAppSignature(ctx).also { signature = it }
        return apkSign == AOEIUV020_SIGNATURE
    }

    fun asyncCheckSignature(ctx: Context) {
        Observable.fromCallable {
            Check.checkSignature(ctx)
        }.async().subscribe {
            if (it) {
                return@subscribe
            }
            ctx.alert {
                title = "签名不正确"
                message = "你可能用了假app,"
                neutralPressed("忽略") {
                    Check.ignoreSignatureCheck = true
                }
                positiveButton("酷安") {
                    ctx.browse(Check.RELEASE_COOLAPK)
                }
                negativeButton("Github") {
                    ctx.browse(Check.RELEASE_GITHUB)
                }
            }.show()

        }
    }
}
