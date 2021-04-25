@file:Suppress("DEPRECATION")

package cc.aoeiuv020.panovel.backup

import android.Manifest
import android.app.ProgressDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.MenuItem
import android.widget.RadioButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import cc.aoeiuv020.panovel.IView
import cc.aoeiuv020.panovel.R
import cc.aoeiuv020.panovel.settings.BackupSettings
import cc.aoeiuv020.panovel.util.loading
import cc.aoeiuv020.panovel.util.notNullOrReport
import cc.aoeiuv020.panovel.util.safelyShow
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_export.*
import org.jetbrains.anko.*


class BackupActivity : AppCompatActivity(), AnkoLogger, IView {
    companion object {
        fun start(ctx: Context) {
            ctx.startActivity<BackupActivity>()
        }
    }

    lateinit var progressDialog: ProgressDialog
    private lateinit var presenter: BackupPresenter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_export)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        initWidget()

        presenter = BackupPresenter()
        presenter.attach(this)
        presenter.start()
    }

    fun getCheckedOption(): Set<BackupOption> {
        val options = mutableSetOf<BackupOption>()
        cbBookshelf.isChecked && options.add(BackupOption.Bookshelf)
        cbBookList.isChecked && options.add(BackupOption.BookList)
        cbProgress.isChecked && options.add(BackupOption.Progress)
        cbSettings.isChecked && options.add(BackupOption.Settings)
        return options
    }

    fun getSelectPath(): Uri = when (rgPath.checkedRadioButtonId) {
        R.id.rbDefaultOldUri -> rbDefaultOldUri.text.toString()
        R.id.rbDefaultNewUri -> rbDefaultNewUri.text.toString()
        R.id.rbOtherPath -> etOtherPath.text.toString()
        else -> throw IllegalStateException("未知错误，")
    }.let {
        Uri.parse(it)
    }

    fun getSelectedId(): Int = rgPath.checkedRadioButtonId

    private fun initWidget() {
        progressDialog = ProgressDialog(this)
        btnImport.setOnClickListener {
            loading(progressDialog, getString(R.string.sImport))
            saveSelected()
            presenter.import()
        }
        btnExport.setOnClickListener {
            loading(progressDialog, getString(R.string.export))
            saveSelected()
            presenter.export()
        }
        btnChoose.setOnClickListener {
            requestFile()
        }
        loadSelected()
        repeat(rgPath.childCount) { index ->
            rgPath.getChildAt(index).setOnClickListener { v ->
                val backupHelper = presenter.getHelper(v.id) ?: return@setOnClickListener
                debug {
                    "backup click ${backupHelper.type}"
                }
                startConfig(backupHelper, index)
            }
        }
    }

    private fun loadSelected() {
        val checkedIndex = if (BackupSettings.checkedButtonIndex == -1) {
            rgPath.childCount - 1
        } else {
            BackupSettings.checkedButtonIndex
        }
        rgPath.check(rgPath.getChildAt(checkedIndex).id)
        cbBookshelf.isChecked = BackupSettings.cbBookshelf
        cbBookList.isChecked = BackupSettings.cbBookList
        cbProgress.isChecked = BackupSettings.cbProgress
        cbSettings.isChecked = BackupSettings.cbSettings
    }

    private fun saveSelected() {
        repeat(rgPath.childCount) { index ->
            val childAt = rgPath.getChildAt(index)
            if (childAt.id == rgPath.checkedRadioButtonId) {
                BackupSettings.checkedButtonIndex = if (index == rgPath.childCount - 1) {
                    -1
                } else {
                    index
                }
            }
        }
        BackupSettings.cbBookshelf = cbBookshelf.isChecked
        BackupSettings.cbBookList = cbBookList.isChecked
        BackupSettings.cbProgress = cbProgress.isChecked
        BackupSettings.cbSettings = cbSettings.isChecked
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when {
            requestCode == 1 -> data?.data?.let { uri ->
                showOtherPath(uri.toString())
            }
            1000 <= requestCode && requestCode < 1000 + rgPath.childCount -> {
                val index = requestCode - 1000
                val radioButton = rgPath.getChildAt(index) as RadioButton
                val backupHelper = presenter.getHelper(radioButton.id).notNullOrReport()
                if (backupHelper.ready()) {
                    radioButton.text = backupHelper.notNullOrReport().configPreview()
                } else {
                    radioButton.text = getString(R.string.backup_click_for_reconfig, backupHelper.type)
                }
                showMessage("配置完成后请重新点击导入或者导出")
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            1 -> {
                showMessage("赋予权限后请重新点击导入或者导出")
            }
        }
    }

    private fun requestFile() {
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Intent(Intent.ACTION_OPEN_DOCUMENT)
        } else {
            Intent(Intent.ACTION_GET_CONTENT)
        }
        intent.type = "*/*"
        try {
            startActivityForResult(intent, 1)
        } catch (e: ActivityNotFoundException) {
            showError(getString(R.string.no_file_explorer), e)
        }
    }

    fun requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
            intent.data = Uri.parse("package:$packageName")
            startActivityForResult(intent, 1)
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            ), 1)
        }
    }

    fun showImportSuccess(result: String) {
        progressDialog.dismiss()
        alert(
                message = result,
                title = "导入完成"
        ).safelyShow()
    }

    fun showExportSuccess(result: String) {
        progressDialog.dismiss()
        alert(
                message = result,
                title = "导出完成"
        ).safelyShow()
    }

    fun showDefaultPath(defaultOldUri: String, defaultNewUri: String) {
        rbDefaultOldUri.text = defaultOldUri
        rbDefaultNewUri.text = defaultNewUri
    }

    fun showOtherPath(defaultOtherUri: String) {
        etOtherPath.setText(defaultOtherUri)
    }

    private val snack: Snackbar by lazy {
        // TODO: 有时候不会弹出，只在收起时闪一下，可能是设置了跟着软键盘弹起的原因，
        Snackbar.make(clRoot, "", Snackbar.LENGTH_SHORT)
    }

    fun showMessage(message: String) {
        snack.setText(message)
        snack.show()
    }

    fun showError(message: String, e: Throwable) {
        progressDialog.dismiss()
        showMessage(message + e.message)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    fun showBackupHint(radioButtonId: Int, test: String) {
        rgPath.find<RadioButton>(radioButtonId).text = test
    }

    fun startConfig(backupHelper: BackupHelper) {
        repeat(rgPath.childCount) { index ->
            val childAt = rgPath.getChildAt(index)
            if (childAt.id == rgPath.checkedRadioButtonId) {
                startConfig(backupHelper, index)
            }
        }
    }

    private fun startConfig(backupHelper: BackupHelper, index: Int) {
        debug {
            "startConfig ${backupHelper.type}"
        }
        startActivityForResult(Intent(ctx, backupHelper.configActivity()), 1000 + index)
    }

}
