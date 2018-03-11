package cc.aoeiuv020.panovel.text

import android.view.View
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import cc.aoeiuv020.panovel.R
import cc.aoeiuv020.panovel.api.NovelItem
import cc.aoeiuv020.panovel.local.Bookshelf
import cc.aoeiuv020.panovel.local.Margins
import cc.aoeiuv020.panovel.local.Settings
import cc.aoeiuv020.panovel.util.alertColorPicker
import cc.aoeiuv020.panovel.util.hide
import cc.aoeiuv020.panovel.util.show
import cc.aoeiuv020.reader.AnimationMode
import cc.aoeiuv020.reader.ReaderConfigName
import kotlinx.android.synthetic.main.novel_text_navigation.view.*
import kotlinx.android.synthetic.main.novel_text_read_animation.view.*
import kotlinx.android.synthetic.main.novel_text_read_default.view.*
import kotlinx.android.synthetic.main.novel_text_read_margins.view.*
import kotlinx.android.synthetic.main.novel_text_read_settings.view.*
import kotlinx.android.synthetic.main.novel_text_read_typesetting.view.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.alert
import org.jetbrains.anko.debug

/**
 *
 * Created by AoEiuV020 on 2017.11.20-21:59:26.
 */
class NovelTextNavigation(val view: NovelTextActivity, val novelItem: NovelItem, navigation: View) : AnkoLogger {
    private val mPanelDefault = navigation.panelDefault
    private val mPanelSettings = navigation.panelSettings
    private val mPanelTypesetting = navigation.panelTypesetting
    private val mPanelAnimation = navigation.panelAnimation
    private val mPanelMargins = navigation.panelMargins

    private fun showLayout(view: View) {
        listOf(mPanelDefault, mPanelSettings, mPanelTypesetting, mPanelAnimation, mPanelMargins).forEach {
            it.takeIf { it == view }?.show()
                    ?: it.hide()
        }
    }

    init {
        mPanelDefault.ivContents.setOnClickListener {
            view.showContents()
        }
        mPanelDefault.ivSettings.setOnClickListener {
            showLayout(mPanelSettings)
        }
        mPanelDefault.ivStar.apply {
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
        mPanelDefault.ivDetail.setOnClickListener {
            view.detail()
        }
        mPanelDefault.ivRefresh.apply {
            setOnClickListener {
                view.refreshCurrentChapter()
            }
            setOnLongClickListener {
                view.refreshChapterList()
                true
            }
        }
        mPanelDefault.ivDownload.setOnClickListener {
            view.download()
        }
        mPanelDefault.apply {
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

        mPanelSettings.apply {
            // 设置信息字体大小，
            val messageSize = Settings.messageSize
            debug { "load textSite = $messageSize" }
            messageSizeTextView.text = view.getString(R.string.text_size_placeholders, messageSize)
            messageSizeSeekBar.progress = messageSize - 12
            messageSizeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    val iTextSize = 12 + progress
                    messageSizeTextView.text = view.getString(R.string.text_size_placeholders, iTextSize)
                    view.setMessageSize(iTextSize)
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {
                    val iTextSize = 12 + seekBar.progress
                    Settings.messageSize = iTextSize
                }
            })

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

            // 设置字体，
            llFont.setOnClickListener {
                view.alert(R.string.select_font) {
                    positiveButton(android.R.string.yes) {
                        view.requestFont()
                    }
                    negativeButton(R.string.set_default) {
                        view.resetFont()
                    }
                }.show()
            }

            // 设置背景图，
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
                showLayout(mPanelTypesetting)
                view.fullScreen()
            }

            tvAnimation.setOnClickListener {
                showLayout(mPanelAnimation)
            }

        }

        mPanelTypesetting.apply {
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

            initLayoutMargins(llMargins, Settings.contentMargins, ReaderConfigName.ContentMargins)
            llMargins.llDisplay.hide()

            tvPagination.setOnClickListener {
                showLayout(mPanelMargins)
                initLayoutMargins(mPanelMargins, Settings.paginationMargins, ReaderConfigName.PaginationMargins)
            }
            tvTime.setOnClickListener {
                showLayout(mPanelMargins)
                initLayoutMargins(mPanelMargins, Settings.timeMargins, ReaderConfigName.TimeMargins)
            }
            tvBattery.setOnClickListener {
                showLayout(mPanelMargins)
                initLayoutMargins(mPanelMargins, Settings.batteryMargins, ReaderConfigName.BatteryMargins)
            }
            tvBookName.setOnClickListener {
                showLayout(mPanelMargins)
                initLayoutMargins(mPanelMargins, Settings.bookNameMargins, ReaderConfigName.BookNameMargins)
            }
            tvChapterName.setOnClickListener {
                showLayout(mPanelMargins)
                initLayoutMargins(mPanelMargins, Settings.chapterNameMargins, ReaderConfigName.ChapterNameMargins)
            }
        }

        mPanelAnimation.apply {
            val maxSpeed = 3f
            val animationSpeed: Float = Settings.animationSpeed
            tvAnimationSpeed.text = view.getString(R.string.animation_speed_placeholder, animationSpeed)
            sbAnimationSpeed.let { sb ->
                sb.progress = (animationSpeed / maxSpeed * sb.max).toInt()
                sb.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

                    private fun progressToSpeed(seekBar: SeekBar) = maxSpeed / seekBar.max * seekBar.progress

                    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                        tvAnimationSpeed.text = view.getString(R.string.animation_speed_placeholder, progressToSpeed(seekBar))
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar?) {
                    }

                    override fun onStopTrackingTouch(seekBar: SeekBar) {
                        progressToSpeed(seekBar).let {
                            view.setAnimationSpeed(it)
                            Settings.animationSpeed = it
                        }
                    }

                })
            }

            rgAnimationMode.check(when (Settings.animationMode) {
                AnimationMode.SIMPLE -> R.id.rbSimple
                AnimationMode.SIMULATION -> R.id.rbSimulation
                AnimationMode.COVER -> R.id.rbCover
                AnimationMode.SLIDE -> R.id.rbSlide
                AnimationMode.NONE -> R.id.rbNone
                AnimationMode.SCROLL -> R.id.rbScroll
            })
            rgAnimationMode.setOnCheckedChangeListener { _, checkedId ->
                val animationMode = when (checkedId) {
                    R.id.rbSimple -> AnimationMode.SIMPLE
                    R.id.rbSimulation -> AnimationMode.SIMULATION
                    R.id.rbCover -> AnimationMode.COVER
                    R.id.rbSlide -> AnimationMode.SLIDE
                    R.id.rbNone -> AnimationMode.NONE
                    R.id.rbScroll -> AnimationMode.SCROLL
                    else -> AnimationMode.SIMPLE // 不存在的，
                }
                val oldAnimationMode = Settings.animationMode
                if (oldAnimationMode != animationMode) {
                    Settings.animationMode = animationMode
                    view.setAnimationMode(animationMode, oldAnimationMode)
                }
            }
        }
    }

    private fun initLayoutMargins(llMargins: LinearLayout, margins: Margins, name: ReaderConfigName) {
        debug {
            "$name: $margins"
        }
        llMargins.apply {
            val display = margins.enabled
            cbDisplay.setOnCheckedChangeListener(null)
            cbDisplay.isChecked = display
            cbDisplay.setOnCheckedChangeListener { _, isChecked ->
                margins.enabled = isChecked
                view.setMargins(margins, name)
            }

            val spacingListener = object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    if (!fromUser) {
                        return
                    }
                    // 从-1开始，
                    val value = progress - 1
                    var tv: TextView? = null
                    when (seekBar) {
                        leftSpacingSeekBar -> {
                            margins.left = value
                            tv = leftSpacingTextView
                        }
                        rightSpacingSeekBar -> {
                            margins.right = value
                            tv = rightSpacingTextView
                        }
                        topSpacingSeekBar -> {
                            margins.top = value
                            tv = topSpacingTextView
                        }
                        bottomSpacingSeekBar -> {
                            margins.bottom = value
                            tv = bottomSpacingTextView
                        }
                    }
                    view.setMargins(margins, name)
                    tv?.text = view.getString(R.string.spacing_placeholder, value)
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {
                    val value = seekBar.progress - 1
                    when (seekBar) {
                        leftSpacingSeekBar -> margins.left = value
                        rightSpacingSeekBar -> margins.right = value
                        topSpacingSeekBar -> margins.top = value
                        bottomSpacingSeekBar -> margins.bottom = value
                    }
                }
            }

            // 设置左间距，
            val leftSpacing = margins.left
            leftSpacingTextView.text = view.getString(R.string.spacing_placeholder, leftSpacing)
            leftSpacingSeekBar.progress = leftSpacing
            leftSpacingSeekBar.setOnSeekBarChangeListener(spacingListener)

            // 设置右间距，
            val rightSpacing = margins.right
            rightSpacingTextView.text = view.getString(R.string.spacing_placeholder, rightSpacing)
            rightSpacingSeekBar.progress = rightSpacing
            rightSpacingSeekBar.setOnSeekBarChangeListener(spacingListener)

            // 设置上间距，
            val topSpacing = margins.top
            topSpacingTextView.text = view.getString(R.string.spacing_placeholder, topSpacing)
            topSpacingSeekBar.progress = topSpacing
            topSpacingSeekBar.setOnSeekBarChangeListener(spacingListener)

            // 设置下间距，
            val bottomSpacing = margins.bottom
            bottomSpacingTextView.text = view.getString(R.string.spacing_placeholder, bottomSpacing)
            bottomSpacingSeekBar.progress = bottomSpacing
            bottomSpacingSeekBar.setOnSeekBarChangeListener(spacingListener)
        }
    }

    fun reset(currentTextCount: Int, currentTextProgress: Int) {
        showLayout(mPanelDefault)

        mPanelDefault.sbTextProgress.apply {
            max = currentTextCount
            progress = currentTextProgress
        }
    }
}