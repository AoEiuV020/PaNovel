package cc.aoeiuv020.panovel.text

import android.view.View
import android.widget.SeekBar
import cc.aoeiuv020.panovel.R
import cc.aoeiuv020.panovel.api.NovelItem
import cc.aoeiuv020.panovel.local.Bookshelf
import cc.aoeiuv020.panovel.local.Settings
import cc.aoeiuv020.panovel.util.alertColorPicker
import cc.aoeiuv020.panovel.util.hide
import cc.aoeiuv020.panovel.util.show
import kotlinx.android.synthetic.main.novel_text_navigation.view.*
import kotlinx.android.synthetic.main.novel_text_read_settings.view.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug

/**
 *
 * Created by AoEiuV020 on 2017.11.20-21:59:26.
 */
class NovelTextNavigation(val view: NovelTextActivity, val novelItem: NovelItem, navigation: View) : AnkoLogger {
    private val llDefault = navigation.llDefault
    private val llSettings = navigation.llSettings

    init {
        llDefault.ivContents.setOnClickListener {
            view.showContents()
        }
        llDefault.ivSettings.setOnClickListener {
            llSettings.show()
            llDefault.hide()
        }
        llDefault.ivStar.apply {
            isChecked = Bookshelf.contains(novelItem)
            setOnClickListener {
                toggle()
                if (isChecked) {
                    Bookshelf.add(novelItem)
                } else {
                    Bookshelf.remove(novelItem)
                }
            }
        }
        llDefault.ivDetail.setOnClickListener {
            view.detail()
        }
        llDefault.ivDownload.setOnClickListener {
            view.download()
        }

        llSettings.apply {
            // 设置字体大小，
            val textSize = Settings.textSize
            debug { "load textSite = $textSize" }
            textSizeTextView.text = view.getString(R.string.text_size_placeholders, textSize)
            textSizeSeekBar.progress = textSize - 12
            textSizeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    val iTextSize = 12 + progress
                    textSizeTextView.text = view.getString(R.string.text_size_placeholders, iTextSize)
                    view.setTextSize(iTextSize)
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {
                    val iTextSize = 12 + seekBar.progress
                    Settings.textSize = iTextSize
                }
            })

            // 设置行间距，
            val lineSpacing = Settings.lineSpacing
            lineSpacingTextView.text = view.getString(R.string.line_spacing_placeholder, lineSpacing)
            lineSpacingSeekBar.progress = lineSpacing
            lineSpacingSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    lineSpacingTextView.text = view.getString(R.string.line_spacing_placeholder, progress)
                    view.setLineSpacing(progress)
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {
                    Settings.lineSpacing = seekBar.progress
                }
            })

            // 设置段间距，
            val paragraphSpacing = Settings.paragraphSpacing
            paragraphSpacingTextView.text = view.getString(R.string.paragraph_spacing_placeholder, paragraphSpacing)
            paragraphSpacingSeekBar.progress = paragraphSpacing
            paragraphSpacingSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    paragraphSpacingTextView.text = view.getString(R.string.paragraph_spacing_placeholder, progress)
                    view.setParagraphSpacing(progress)
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {
                    Settings.paragraphSpacing = seekBar.progress
                }
            })

            // 设置背景色，
            val backgroundColor = Settings.backgroundColor
            view.setBackgroundColor(backgroundColor)
            backgroundColorTextView.text = view.getString(R.string.background_color_placeholder, backgroundColor)
            backgroundColorTextView.setOnClickListener {
                view.alertColorPicker(Settings.backgroundColor) { color ->
                    Settings.backgroundColor = color
                    backgroundColorTextView.text = view.getString(R.string.background_color_placeholder, color)
                    view.setBackgroundColor(color)
                }
            }

            // 设置文字颜色，
            textColorTextView.text = view.getString(R.string.text_color_placeholder, Settings.textColor)
            textColorTextView.setOnClickListener {
                view.alertColorPicker(Settings.textColor) { color ->
                    Settings.textColor = color
                    textColorTextView.text = view.getString(R.string.text_color_placeholder, color)
                    view.setTextColor(color)
                }
            }

        }
    }

    fun reset() {
        llDefault.show()
        llSettings.hide()
    }
}