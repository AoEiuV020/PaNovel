package cc.aoeiuv020.panovel.text

import android.view.View
import android.widget.SeekBar
import android.widget.TextView
import cc.aoeiuv020.pager.AnimMode
import cc.aoeiuv020.panovel.R
import cc.aoeiuv020.panovel.api.NovelItem
import cc.aoeiuv020.panovel.local.Bookshelf
import cc.aoeiuv020.panovel.local.Settings
import cc.aoeiuv020.panovel.util.alertColorPicker
import cc.aoeiuv020.panovel.util.hide
import cc.aoeiuv020.panovel.util.show
import kotlinx.android.synthetic.main.novel_text_navigation.view.*
import kotlinx.android.synthetic.main.novel_text_read_animation.view.*
import kotlinx.android.synthetic.main.novel_text_read_default.view.*
import kotlinx.android.synthetic.main.novel_text_read_settings.view.*
import kotlinx.android.synthetic.main.novel_text_read_typesetting.view.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug

/**
 *
 * Created by AoEiuV020 on 2017.11.20-21:59:26.
 */
class NovelTextNavigation(val view: NovelTextActivity, val novelItem: NovelItem, navigation: View) : AnkoLogger {
    private val llDefault = navigation.llDefault
    private val llSettings = navigation.llSettings
    private val llTypesetting = navigation.llTypesetting
    private val llAnimation = navigation.llAnimation

    private fun showLayout(view: View) {
        listOf(llDefault, llSettings, llTypesetting, llAnimation).forEach {
            it.takeIf { it == view }?.show()
                    ?: it.hide()
        }
    }

    init {
        llDefault.ivContents.setOnClickListener {
            view.showContents()
        }
        llDefault.ivSettings.setOnClickListener {
            showLayout(llSettings)
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
        llDefault.ivRefresh.apply {
            setOnClickListener {
                view.refreshCurrentChapter()
            }
            setOnLongClickListener {
                view.refreshChapterList()
                true
            }
        }
        llDefault.ivDownload.setOnClickListener {
            view.download()
        }
        llDefault.apply {
            tvPreviousChapter.setOnClickListener {
                view.previousChapter()
            }
            tvNextChapter.setOnClickListener {
                view.nextChapter()
            }
            sbTextProgress.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        view.setTextProgress(progress)
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {
                }
            })
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

            // 设置背景图，
            view.setBackgroundImage(Settings.backgroundImage)
            lBackgroundImage.setOnClickListener {
                view.requestBackgroundImage()
            }

            // 设置背景色，
            val backgroundColor = Settings.backgroundColor
            view.setBackgroundColor(backgroundColor)
            backgroundColorTextView.text = view.getString(R.string.background_color_placeholder, backgroundColor)
            lBackgroundColor.setOnClickListener {
                view.alertColorPicker(Settings.backgroundColor) { color ->
                    Settings.backgroundColor = color
                    Settings.backgroundImage = null
                    backgroundColorTextView.text = view.getString(R.string.background_color_placeholder, color)
                    view.setBackgroundColor(color, true)
                }
            }

            // 设置文字颜色，
            textColorTextView.text = view.getString(R.string.text_color_placeholder, Settings.textColor)
            lTextColor.setOnClickListener {
                view.alertColorPicker(Settings.textColor) { color ->
                    Settings.textColor = color
                    textColorTextView.text = view.getString(R.string.text_color_placeholder, color)
                    view.setTextColor(color)
                }
            }

            tvTypesetting.setOnClickListener {
                showLayout(this@NovelTextNavigation.llTypesetting)
            }

            tvAnimation.setOnClickListener {
                showLayout(this@NovelTextNavigation.llAnimation)
            }

        }

        llTypesetting.apply {

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

            val spacingListener = object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    var tv: TextView? = null
                    when (seekBar) {
                        leftSpacingSeekBar -> {
                            view.setMargins(left = progress)
                            tv = leftSpacingTextView
                        }
                        rightSpacingSeekBar -> {
                            view.setMargins(right = progress)
                            tv = rightSpacingTextView
                        }
                        topSpacingSeekBar -> {
                            view.setMargins(top = progress)
                            tv = topSpacingTextView
                        }
                        bottomSpacingSeekBar -> {
                            view.setMargins(bottom = progress)
                            tv = bottomSpacingTextView
                        }
                    }
                    tv?.text = view.getString(R.string.spacing_placeholder, progress)
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {
                    when (seekBar) {
                        leftSpacingSeekBar -> Settings.leftSpacing = seekBar.progress
                        rightSpacingSeekBar -> Settings.rightSpacing = seekBar.progress
                        topSpacingSeekBar -> Settings.topSpacing = seekBar.progress
                        bottomSpacingSeekBar -> Settings.bottomSpacing = seekBar.progress
                    }
                }
            }

            // 设置左间距，
            val leftSpacing = Settings.leftSpacing
            leftSpacingTextView.text = view.getString(R.string.spacing_placeholder, leftSpacing)
            leftSpacingSeekBar.progress = leftSpacing
            leftSpacingSeekBar.setOnSeekBarChangeListener(spacingListener)

            // 设置右间距，
            val rightSpacing = Settings.rightSpacing
            rightSpacingTextView.text = view.getString(R.string.spacing_placeholder, rightSpacing)
            rightSpacingSeekBar.progress = rightSpacing
            rightSpacingSeekBar.setOnSeekBarChangeListener(spacingListener)

            // 设置上间距，
            val topSpacing = Settings.topSpacing
            topSpacingTextView.text = view.getString(R.string.spacing_placeholder, topSpacing)
            topSpacingSeekBar.progress = topSpacing
            topSpacingSeekBar.setOnSeekBarChangeListener(spacingListener)

            // 设置下间距，
            val bottomSpacing = Settings.bottomSpacing
            bottomSpacingTextView.text = view.getString(R.string.spacing_placeholder, bottomSpacing)
            bottomSpacingSeekBar.progress = bottomSpacing
            bottomSpacingSeekBar.setOnSeekBarChangeListener(spacingListener)
        }

        llAnimation.apply {
            llRoot.check(when (Settings.animMode) {
                null -> R.id.rbSimple
                AnimMode.SIMULATION -> R.id.rbSimulation
                AnimMode.COVER -> R.id.rbCover
                AnimMode.SLIDE -> R.id.rbSlide
                AnimMode.NONE -> R.id.rbNone
                AnimMode.SCROLL -> R.id.rbScroll
            })
            llRoot.setOnCheckedChangeListener { group, checkedId ->
                val animMode = when (checkedId) {
                    R.id.rbSimple -> null
                    R.id.rbSimulation -> AnimMode.SIMULATION
                    R.id.rbCover -> AnimMode.COVER
                    R.id.rbSlide -> AnimMode.SLIDE
                    R.id.rbNone -> AnimMode.NONE
                    R.id.rbScroll -> AnimMode.SCROLL
                    else -> null // 不存在的，
                }
                val oldAnimMode = Settings.animMode
                if (oldAnimMode != animMode) {
                    Settings.animMode = animMode
                    view.setAnimMode(animMode, oldAnimMode)
                }
            }
        }
    }

    fun reset(currentTextCount: Int, currentTextProgress: Int) {
        showLayout(llDefault)

        llDefault.sbTextProgress.apply {
            max = currentTextCount
            progress = currentTextProgress
        }
    }
}