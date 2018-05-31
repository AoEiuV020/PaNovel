package cc.aoeiuv020.panovel.settings

import android.os.Bundle
import android.preference.Preference
import android.preference.PreferenceFragment
import cc.aoeiuv020.panovel.R
import cc.aoeiuv020.panovel.data.DataManager
import cc.aoeiuv020.panovel.report.Reporter
import org.jetbrains.anko.*

/**
 *
 * Created by AoEiuV020 on 2017.11.25-16:15:29.
 */
class CacheClearPreferenceFragment : PreferenceFragment(), AnkoLogger {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.pref_cache_clear)
        setHasOptionsMenu(true)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val map = mapOf("cache" to {
            DataManager.cleanAllCache()
        }, "bookshelf" to {
            DataManager.cleanBookshelf()
        }, "book_list" to {
            DataManager.cleanBookList()
        }, "history" to {
            DataManager.cleanHistory()
        })

        val listener: Preference.OnPreferenceClickListener = Preference.OnPreferenceClickListener { p ->
                        map[p.key]?.also { clear ->
                            alert(p.title.toString(), getString(R.string.confirm_clear)) {
                                yesButton {
                                    val dialog = indeterminateProgressDialog(getString(R.string.removing, p.title))
                                    dialog.show()
                                    doAsync({ e ->
                                        val message = "清除失败，"
                                        Reporter.post(message, e)
                                        error(message, e)
                                    }) {
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
