@file:Suppress("DEPRECATION")

package cc.aoeiuv020.panovel.settings

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.preference.PreferenceFragment
import android.preference.TwoStatePreference
import android.provider.Settings
import androidx.core.app.ActivityCompat
import cc.aoeiuv020.anull.notNull
import cc.aoeiuv020.panovel.R
import cc.aoeiuv020.panovel.data.DataManager
import cc.aoeiuv020.panovel.util.Pref
import cc.aoeiuv020.panovel.util.attach
import org.jetbrains.anko.act
import org.jetbrains.anko.ctx


/**
 *
 * Created by AoEiuV020 on 2017.10.15-19:42:24.
 */

abstract class BasePreferenceFragment(
        private val prefObj: Pref,
        private val prefId: Int
) : PreferenceFragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 绑定这个Pref对象，配置相同数据园，但是默认配置没法同步，两边都要写，
        attach(prefObj)
        addPreferencesFromResource(prefId)
        setHasOptionsMenu(true)
    }
}

class AdPreferenceFragment : BasePreferenceFragment(AdSettings, R.xml.pref_ad) {
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // 实现广告启用时不能直接禁用，
        val adEnabledPreference = findPreference("adEnabled") as TwoStatePreference
        adEnabledPreference.apply {
            isEnabled = !AdSettings.adEnabled
        }
    }
}

class GeneralPreferenceFragment : BasePreferenceFragment(GeneralSettings, R.xml.pref_general)

class ListPreferenceFragment : BasePreferenceFragment(ListSettings, R.xml.pref_list)

class ReaderPreferenceFragment : BasePreferenceFragment(ReaderSettings, R.xml.pref_read)

class DownloadPreferenceFragment : BasePreferenceFragment(DownloadSettings, R.xml.pref_download)

class LocationPreferenceFragment : BasePreferenceFragment(LocationSettings, R.xml.pref_location),
    SharedPreferences.OnSharedPreferenceChangeListener {
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            // 缓存目录修改立即生效，
            "cacheLocation" -> DataManager.resetCacheLocation(activity.notNull())
        }
    }

    private fun requestPermissions() {
        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager())) {
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
            intent.data = Uri.parse("package:${ctx.packageName}")
            startActivityForResult(intent, 1)
        } else {
            ActivityCompat.requestPermissions(act, arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            ), 1)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        requestPermissions()
    }

    override fun onResume() {
        super.onResume()
        preferenceManager.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        preferenceManager.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }
}

class ServerPreferenceFragment : BasePreferenceFragment(ServerSettings, R.xml.pref_server)

class OthersPreferenceFragment : BasePreferenceFragment(OtherSettings, R.xml.pref_others)

class InterfacePreferenceFragment : BasePreferenceFragment(InterfaceSettings, R.xml.pref_interface)
