package cc.aoeiuv020.panovel.settings

import cc.aoeiuv020.panovel.util.Delegates
import cc.aoeiuv020.panovel.util.Pref

object SiteSettings : Pref {
    override val name: String
        get() = "Site"
    var cachedVersion: Int by Delegates.int(0)
}
