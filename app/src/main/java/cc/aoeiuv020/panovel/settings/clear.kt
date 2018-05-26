package cc.aoeiuv020.panovel.settings

import android.os.Bundle
import android.preference.Preference
import android.preference.PreferenceFragment
import android.view.View
import cc.aoeiuv020.panovel.R
import org.jetbrains.anko.*

/**
 *
 * Created by AoEiuV020 on 2017.11.25-16:15:29.
 */
class CacheClearPreferenceFragment : PreferenceFragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 绑定这个Pref对象，配置相同数据园，但是默认配置没法同步，两边都要写，
        addPreferencesFromResource(R.xml.pref_cache_clear)
        setHasOptionsMenu(true)
    }
    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val map = mapOf("cache" to { TODO("清除缓存，") },
                "history" to { TODO("历史记录不需要清除了，改点别的，") })

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
