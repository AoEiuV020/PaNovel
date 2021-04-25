package cc.aoeiuv020.panovel.backup.webdav

import android.app.Activity
import cc.aoeiuv020.panovel.backup.BackupHelper
import cc.aoeiuv020.panovel.util.Delegates
import cc.aoeiuv020.panovel.util.notNullOrReport
import com.thegrizzlylabs.sardineandroid.Sardine
import com.thegrizzlylabs.sardineandroid.impl.OkHttpSardine
import okhttp3.HttpUrl
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import java.io.File


/**
 * Created by AoEiuV020 on 2021.04.25-12:35:51.
 */
class BackupWebDavHelper : BackupHelper, AnkoLogger {
    override val type: String
        get() = "WebDav"
    var server: String by Delegates.string("")
    var fileName: String by Delegates.string("PaNovel-Backup.zip")
    var username: String by Delegates.string("")
    var password: String by Delegates.string("")

    override fun ready(): Boolean {
        return server.takeIf { it.isNotBlank() }?.takeIf { HttpUrl.parse(it) != null } != null
                && fileName.takeIf { it.isNotBlank() } != null
                && username.takeIf { it.isNotBlank() } != null
                && password.takeIf { it.isNotBlank() } != null
    }

    override fun configPreview(): String {
        if (!ready()) {
            error("没配置好，")
        }
        return getUrl()
    }

    private fun getUrl(): String {
        return HttpUrl.parse(server).notNullOrReport().newBuilder()
                .username(username)
                .password(password)
                .addPathSegment(fileName)
                .build()
                .toString()
    }

    override fun configActivity(): Class<out Activity> {
        return BackupWebDavConfigActivity::class.java
    }

    override fun restore(tempFile: File) {
        info {
            "import ${configPreview()}, file: $tempFile"
        }
        val sardine: Sardine = initWebDav()
        sardine.get(configPreview()).use { input ->
            tempFile.outputStream().use { output ->
                input.copyTo(output)
                output.flush()
            }
        }
    }

    override fun backup(tempFile: File) {
        info {
            "export ${configPreview()}, file: $tempFile"
        }
        val sardine: Sardine = initWebDav()
        sardine.put(configPreview(), tempFile, "application/zip")
    }

    private fun initWebDav(): Sardine {
        val sardine: Sardine = OkHttpSardine()
        sardine.setCredentials(username, password)
        if (!sardine.exists(server)) {
            //不存在目录即创建
            sardine.createDirectory(server);
        }

        return sardine
    }
}