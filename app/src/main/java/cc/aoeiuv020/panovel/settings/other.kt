package cc.aoeiuv020.panovel.settings

import cc.aoeiuv020.panovel.share.Expiration
import cc.aoeiuv020.panovel.util.Delegates
import cc.aoeiuv020.panovel.util.Pref

/**
 * Created by AoEiuV020 on 2018.05.26-17:14:31.
 */
object OtherSettings : Pref {
    override val name: String
        get() = "Other"

    /**
     * 书单分享后网上保存的时限，
     */
    var shareExpiration: Expiration by Delegates.enum(Expiration.NONE)
    var reportCrash: Boolean by Delegates.boolean(true)
    var subscribeNovelUpdate: Boolean by Delegates.boolean(true)

}