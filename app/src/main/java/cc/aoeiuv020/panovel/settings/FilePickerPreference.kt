package cc.aoeiuv020.panovel.settings

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.preference.Preference
import android.util.AttributeSet
import android.view.View

import com.github.angads25.filepicker.R
import com.github.angads25.filepicker.controller.DialogSelectionListener
import com.github.angads25.filepicker.model.DialogConfigs
import com.github.angads25.filepicker.model.DialogProperties
import com.github.angads25.filepicker.view.FilePickerDialog

import java.io.File

/**
 * copy froom [com.github.angads25.filepicker.view.FilePickerPreference]
 */
@Suppress("RedundantOverride", "MemberVisibilityCanBePrivate", "unused", "SpellCheckingInspection", "LocalVariableName", "RemoveEmptySecondaryConstructorBody")
class FilePickerPreference : Preference, DialogSelectionListener, Preference.OnPreferenceClickListener {
    private var mDialog: FilePickerDialog? = null
    private var properties: DialogProperties = DialogProperties()
    private var titleText: String? = null

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
        initProperties(attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
        initProperties(attrs)
    }

    private fun init() {
        onPreferenceClickListener = this
        properties.offset = File(defaultPath)
    }

    override fun onGetDefaultValue(a: TypedArray, index: Int): Any {
        return super.onGetDefaultValue(a, index)
    }

    override fun onSetInitialValue(restorePersistedValue: Boolean, defaultValue: Any?) {
        // 当前值为空时给个全局的默认值，是app在sd卡的私有目录，
        val defaultString: String = (defaultValue as? String) ?: defaultPath
        val value = if (restorePersistedValue) this.getPersistedString(defaultString) else defaultString
        properties.offset = File(value.split(':').first())
        setProperties(properties)
    }

    private val defaultPath: String
        // 默认根目录是/mnt，这库有判断路径要是根目录开头才能用，所以不能通过getExternalFilesDir获取，
        @SuppressLint("SdCardPath")
        get() = (File("/mnt/sdcard/Android/data/${context.packageName}/files")
                .apply { exists() || mkdirs() }
                .takeIf { it.canWrite() }
                ?: context.filesDir
                ).absolutePath

    override fun onBindView(view: View) {
        super.onBindView(view)
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        if (mDialog == null || !mDialog!!.isShowing) {
            return superState
        }

        val myState = SavedState(superState)
        myState.dialogBundle = mDialog!!.onSaveInstanceState()
        return myState
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state == null || state !is SavedState) {
            super.onRestoreInstanceState(state)
            return
        }
        val myState = state as SavedState?
        super.onRestoreInstanceState(myState!!.superState)
        showDialog(myState.dialogBundle)
    }

    private fun showDialog(state: Bundle?) {
        mDialog = FilePickerDialog(context)
        setProperties(properties)
        mDialog!!.setDialogSelectionListener(this)
        if (state != null) {
            mDialog!!.onRestoreInstanceState(state)
        }
        mDialog!!.setTitle(titleText)
        mDialog!!.show()
    }

    override fun onSelectedFilePaths(files: Array<String>) {
        properties.offset = File(files.first())
        val dFiles = files.joinToString(":")
        if (isPersistent) {
            persistString(dFiles)
        }
        callChangeListener(dFiles)
    }

    override fun onPreferenceClick(preference: Preference): Boolean {
        showDialog(null)
        return false
    }

    fun setProperties(properties: DialogProperties) {
        this.properties = properties
        mDialog?.properties = properties
    }

    private class SavedState : Preference.BaseSavedState {
        lateinit var dialogBundle: Bundle

        constructor(source: Parcel) : super(source) {
            dialogBundle = source.readBundle(javaClass.classLoader)
        }

        override fun writeToParcel(dest: Parcel, flags: Int) {
            super.writeToParcel(dest, flags)
            dest.writeBundle(dialogBundle)
        }

        constructor(superState: Parcelable) : super(superState) {}

        companion object {

            @JvmField
            val CREATOR: Parcelable.Creator<SavedState> = object : Parcelable.Creator<SavedState> {
                override fun createFromParcel(`in`: Parcel): SavedState {
                    return SavedState(`in`)
                }

                override fun newArray(size: Int): Array<SavedState?> {
                    return arrayOfNulls(size)
                }
            }
        }
    }

    private fun initProperties(attrs: AttributeSet) {
        val tarr = context.theme.obtainStyledAttributes(attrs, R.styleable.FilePickerPreference, 0, 0)
        val N = tarr.indexCount
        for (i in 0 until N) {
            val attr = tarr.getIndex(i)
            if (attr == R.styleable.FilePickerPreference_selection_mode) {
                properties.selection_mode = tarr.getInteger(R.styleable.FilePickerPreference_selection_mode, DialogConfigs.SINGLE_MODE)
            } else if (attr == R.styleable.FilePickerPreference_selection_type) {
                properties.selection_type = tarr.getInteger(R.styleable.FilePickerPreference_selection_type, DialogConfigs.FILE_SELECT)
            } else if (attr == R.styleable.FilePickerPreference_root_dir) {
                val root_dir = tarr.getString(R.styleable.FilePickerPreference_root_dir)
                if (root_dir != null && root_dir != "") {
                    properties.root = File(root_dir)
                }
            } else if (attr == R.styleable.FilePickerPreference_error_dir) {
                val error_dir = tarr.getString(R.styleable.FilePickerPreference_error_dir)
                if (error_dir != null && error_dir != "") {
                    properties.error_dir = File(error_dir)
                }
            } else if (attr == R.styleable.FilePickerPreference_offset_dir) {
                val offset_dir = tarr.getString(R.styleable.FilePickerPreference_offset_dir)
                if (offset_dir != null && offset_dir != "") {
                    properties.offset = File(offset_dir)
                }
            } else if (attr == R.styleable.FilePickerPreference_extensions) {
                val extensions = tarr.getString(R.styleable.FilePickerPreference_extensions)
                if (extensions != null && extensions != "") {
                    properties.extensions = extensions.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                }
            } else if (attr == R.styleable.FilePickerPreference_title_text) {
                titleText = tarr.getString(R.styleable.FilePickerPreference_title_text)
            }
        }
        tarr.recycle()
    }
}
