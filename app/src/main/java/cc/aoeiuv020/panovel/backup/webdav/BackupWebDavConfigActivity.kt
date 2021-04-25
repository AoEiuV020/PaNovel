package cc.aoeiuv020.panovel.backup.webdav

import android.os.Bundle
import android.view.MenuItem
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import cc.aoeiuv020.panovel.R
import cc.aoeiuv020.panovel.util.notNullOrReport
import kotlinx.android.synthetic.main.activity_backup_web_dav_config.*
import okhttp3.HttpUrl
import org.jetbrains.anko.browse
import org.jetbrains.anko.ctx
import org.jetbrains.anko.toast

class BackupWebDavConfigActivity : AppCompatActivity() {
    private val backupHelper = BackupWebDavHelper()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_backup_web_dav_config)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        btnSave.setOnClickListener {
            if (!checkInput()) {
                return@setOnClickListener
            }
            backupHelper.server = getInput(llServer)
            backupHelper.fileName = getInput(llFileName)
            backupHelper.username = getInput(llUsername)
            getInput(llPassword).takeIf { it.isNotEmpty() }?.let {
                backupHelper.password = it
            }
            finish()
        }
        setInput(llServer, backupHelper.server)
        setInput(llFileName, backupHelper.fileName)
        setInput(llUsername, backupHelper.username)
        if (backupHelper.password.isNotEmpty()) {
            setInputHint(llPassword, "密码不变")
        }

        tvJianguoyun.setOnClickListener { v ->
            browse("https://blog.jianguoyun.com/?p=2748")
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun checkInput(): Boolean {
        getInput(llServer).takeIf { it.isNotBlank() }?.let { HttpUrl.parse(it) }?.also {
            if (it.host() == "dav.jianguoyun.com" && (it.encodedPath() == "/dav" || it.encodedPath() == "/dav/")) {
                AlertDialog.Builder(ctx)
                        .setMessage("坚果云根目录不允许存放文件，请指定子目录，如：\nhttps://dav.jianguoyun.com/dav/panovel")
                        .show()
                return false
            }
        } ?: return false.also {
            toast("服务器地址不合法")
        }
        getInput(llFileName).takeIf { it.isNotEmpty() } ?: return false.also {
            toast("文件名不能为空")
        }
        getInput(llUsername).takeIf { it.isNotEmpty() } ?: return false.also {
            toast("用户名不能为空")
        }
        getInput(llPassword).takeIf { it.isNotEmpty() || backupHelper.password.isNotEmpty() }
                ?: return false.also {
                    toast("密码不能为空")
                }
        return true
    }

    private fun getInput(layout: LinearLayout): String {
        return (layout.getChildAt(1) as EditText).text.notNullOrReport().toString()
    }

    private fun setInput(layout: LinearLayout, text: String) {
        return (layout.getChildAt(1) as EditText).setText(text)
    }

    private fun setInputHint(layout: LinearLayout, text: String) {
        return (layout.getChildAt(1) as EditText).setHint(text)
    }
}