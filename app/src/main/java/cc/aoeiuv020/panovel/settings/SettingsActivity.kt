package cc.aoeiuv020.panovel.settings

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.preference.PreferenceActivity
import android.preference.PreferenceFragment
import android.view.MenuItem
import cc.aoeiuv020.panovel.R
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.startActivity

/**
 *
 * Created by AoEiuV020 on 2017.10.15-19:11:34.
 */
class SettingsActivity : AppCompatPreferenceActivity(), AnkoLogger {
    companion object {
        fun start(context: Context) {
            context.startActivity<SettingsActivity>()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)


    }

    override fun onIsMultiPane(): Boolean
            = resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK >= Configuration.SCREENLAYOUT_SIZE_XLARGE

    override fun onBuildHeaders(target: List<PreferenceActivity.Header>) {
        loadHeadersFromResource(R.xml.pref_headers, target)
    }

    override fun isValidFragment(fragmentName: String): Boolean {
        return PreferenceFragment::class.java.name == fragmentName
                || GeneralPreferenceFragment::class.java.name == fragmentName
                || BookshelfPreferenceFragment::class.java.name == fragmentName
                || ReadPreferenceFragment::class.java.name == fragmentName
                || OthersPreferenceFragment::class.java.name == fragmentName
                || CacheClearPreferenceFragment::class.java.name == fragmentName
                || AboutFragment::class.java.name == fragmentName
                || DisclaimerFragment::class.java.name == fragmentName
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }
}
