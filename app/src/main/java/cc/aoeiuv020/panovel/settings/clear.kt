package cc.aoeiuv020.panovel.settings

import android.os.Bundle
import android.preference.Preference
import android.view.View
import cc.aoeiuv020.panovel.R
import cc.aoeiuv020.panovel.local.Cache
import cc.aoeiuv020.panovel.local.History
import org.jetbrains.anko.*

/**
 *
 * Created by AoEiuV020 on 2017.11.25-16:15:29.
 */
class CacheClearPreferenceFragment : BasePreferenceFragment(R.xml.pref_cache_clear) {
    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val map = mapOf("cache" to { Cache.clear() },
                "history" to { History.clear() })

        val listener: Preference.OnPreferenceClickListener = Preference.OnPreferenceClickListener { p ->
            map[p.key]?.also { clear ->
                alert(p.title.toString(), getString(R.string.confirm_clear)) {
                    yesButton {
                        val dialog = indeterminateProgressDialog(getString(R.string.removing, p.title))
                        dialog.show()
                        doAsync {
                            clear.invoke()
                            uiThread {
                                dialog.dismiss()
                            }
                        }
                    }
                }.show()
            }
            true
        }
        repeat(preferenceScreen.preferenceCount) {
            val p = preferenceScreen.getPreference(it)
            p.onPreferenceClickListener = listener
        }
    }
}
