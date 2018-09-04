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
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import cc.aoeiuv020.panovel.IView
import cc.aoeiuv020.panovel.R
import cc.aoeiuv020.panovel.util.loading
import cc.aoeiuv020.panovel.util.safelyShow
import kotlinx.android.synthetic.main.activity_export.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.alert
import org.jetbrains.anko.startActivity

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

    private fun initWidget() {
        progressDialog = ProgressDialog(this)
        btnImport.setOnClickListener {
            loading(progressDialog, getString(R.string.sImport))
            presenter.import()
        }
        btnExport.setOnClickListener {
            loading(progressDialog, getString(R.string.export))
            presenter.export()
        }
        btnChoose.setOnClickListener {
            requestFile()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            1 -> data?.data?.let { uri ->
                showOtherPath(uri.toString())
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            1 -> {
                showMessage("赋予权限后请重试，")
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
        ActivityCompat.requestPermissions(this, arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        ), 1)
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

}
