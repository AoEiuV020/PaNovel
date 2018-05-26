package cc.aoeiuv020.panovel.settings

import android.os.Bundle
import android.preference.PreferenceFragment
import android.view.View
import cc.aoeiuv020.panovel.R


/**
 *
 * Created by AoEiuV020 on 2017.10.15-19:42:24.
 */

abstract class BasePreferenceFragment(private val prefId: Int) : PreferenceFragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(prefId)
        setHasOptionsMenu(true)
    }
}

class GeneralPreferenceFragment : BasePreferenceFragment(R.xml.pref_general) {
    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adEnabledPreference = findPreference("ad_enabled") as SwitchPreference
        adEnabledPreference.apply {
            isEnabled = !GeneralSettings.adEnabled
        }
    }
}

class BookshelfPreferenceFragment : BasePreferenceFragment(R.xml.pref_bookshelf)

class ReadPreferenceFragment : BasePreferenceFragment(R.xml.pref_read)

class OthersPreferenceFragment : BasePreferenceFragment(R.xml.pref_others)
