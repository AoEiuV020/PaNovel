package cc.aoeiuv020.panovel.text

import android.content.DialogInterface
import android.support.v7.app.AlertDialog
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import cc.aoeiuv020.panovel.R
import cc.aoeiuv020.panovel.data.entity.Novel
import cc.aoeiuv020.panovel.settings.Margins
import cc.aoeiuv020.panovel.settings.ReaderSettings
import cc.aoeiuv020.panovel.text.NovelTextNavigation.Direction.*
import cc.aoeiuv020.panovel.util.*
import cc.aoeiuv020.reader.AnimationMode
import cc.aoeiuv020.reader.ReaderConfigName
import kotlinx.android.synthetic.main.dialog_seekbar.view.*
import kotlinx.android.synthetic.main.novel_text_navigation.view.*
import kotlinx.android.synthetic.main.novel_text_read_animation.view.*
import kotlinx.android.synthetic.main.novel_text_read_default.view.*
import kotlinx.android.synthetic.main.novel_text_read_margins.view.*
import kotlinx.android.synthetic.main.novel_text_read_margins_item.view.*
import kotlinx.android.synthetic.main.novel_text_read_settings.view.*
import kotlinx.android.synthetic.main.novel_text_read_typesetting.view.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.alert
import org.jetbrains.anko.debug

/**
 *
 * Created by AoEiuV020 on 2017.11.20-21:59:26.
 */
class NovelTextNavigation(val view: NovelTextActivity, val novel: Novel, navigation: View) : AnkoLogger {
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
            view.presenter.loadContents()
        }
        mPanelDefault.ivSettings.setOnClickListener {
            showLayout(mPanelSettings)
            view.fullScreen()
        }
        mPanelDefault.ivStar.apply {
            isChecked = novel.bookshelf
            setOnClickListener {
                toggle()
                view.presenter.updateBookshelf(isChecked)
            }
        }
        mPanelDefault.ivColor.setOnClickListener {
            view.selectColorScheme()
        }
        mPanelDefault.ivColor.setOnLongClickListener {
            view.lastColorScheme()
            true
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
            val messageSize = ReaderSettings.messageSize
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
                    ReaderSettings.messageSize = iTextSize
                }
            })

            // 设置字体大小，
            val textSize = ReaderSettings.textSize
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
                    ReaderSettings.textSize = iTextSize
                }
            })

            // 设置字体，
            tvFont.setOnClickListener {
                view.alert(R.string.select_font) {
                    positiveButton(android.R.string.yes) {
                        view.requestFont()
                    }
                    negativeButton(R.string.set_default) {
                        view.resetFont()
                    }
                }.safelyShow()
            }

            tvTypesetting.setOnClickListener {
                showLayout(mPanelTypesetting)
            }

            tvAnimation.setOnClickListener {
                showLayout(mPanelAnimation)
            }

            // 设置亮度，
            view.setBrightness(ReaderSettings.brightness)
            tvBrightness.setOnClickListener {
                AlertDialog.Builder(view).apply {
                    setTitle(R.string.brightness)
                    val layout = View.inflate(view, R.layout.dialog_seekbar, null)
                    setView(layout)
                    layout.seekBar.apply {
                        progress = ReaderSettings.brightness
                        setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                                // progress 0-255,
                                view.setBrightness(progress)
                            }

                            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                            }

                            override fun onStopTrackingTouch(seekBar: SeekBar) {
                                ReaderSettings.brightness = seekBar.progress
                            }
                        })
                    }
                    setNeutralButton(R.string.follow_system) { _, _ ->
                        view.setBrightnessFollowSystem()
                        // 负数代表亮度跟随系统，
                        ReaderSettings.brightness = -1
                    }
                    // 鬼知道发生了什么，这里简写成lambda就会编译报错，上面的就没问题，
                    @Suppress("ObjectLiteralToLambda")
                    val emptyListener = object : DialogInterface.OnClickListener {
                        override fun onClick(dialog: DialogInterface?, which: Int) {
                        }
                    }
                    setPositiveButton(android.R.string.yes, emptyListener)
                }.create().apply {
                    // 去除对话框的灰背景，
                    window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
                }.safelyShow()
                // 弹对话框时退出全屏，
                view.hide()
            }

            // 设置保持亮屏，
            if (ReaderSettings.keepScreenOn) {
                view.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }

        mPanelTypesetting.apply {
            // 设置行间距，
            val lineSpacing = ReaderSettings.lineSpacing
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
                    ReaderSettings.lineSpacing = seekBar.progress
                }
            })

            // 设置段间距，
            val paragraphSpacing = ReaderSettings.paragraphSpacing
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
                    ReaderSettings.paragraphSpacing = seekBar.progress
                }
            })

            initLayoutMargins(llMargins, ReaderSettings.contentMargins, ReaderConfigName.ContentMargins)
            llMargins.llDisplay.hide()

            tvPagination.setOnClickListener {
                showLayout(mPanelMargins)
                initLayoutMargins(mPanelMargins, ReaderSettings.paginationMargins, ReaderConfigName.PaginationMargins)
            }
            tvTime.setOnClickListener {
                showLayout(mPanelMargins)
                initLayoutMargins(mPanelMargins, ReaderSettings.timeMargins, ReaderConfigName.TimeMargins)
            }
            tvBattery.setOnClickListener {
                showLayout(mPanelMargins)
                initLayoutMargins(mPanelMargins, ReaderSettings.batteryMargins, ReaderConfigName.BatteryMargins)
            }
            tvBookName.setOnClickListener {
                showLayout(mPanelMargins)
                initLayoutMargins(mPanelMargins, ReaderSettings.bookNameMargins, ReaderConfigName.BookNameMargins)
            }
            tvChapterName.setOnClickListener {
                showLayout(mPanelMargins)
                initLayoutMargins(mPanelMargins, ReaderSettings.chapterNameMargins, ReaderConfigName.ChapterNameMargins)
            }
        }

        mPanelAnimation.apply {
            val maxSpeed = 3f
            val animationSpeed: Float = ReaderSettings.animationSpeed
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
                            ReaderSettings.animationSpeed = it
                        }
                    }

                })
            }

            rgAnimationMode.check(when (ReaderSettings.animationMode) {
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
                val oldAnimationMode = ReaderSettings.animationMode
                if (oldAnimationMode != animationMode) {
                    ReaderSettings.animationMode = animationMode
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

            initMarginSeekBar(iLeft, LEFT, margins, name)
            initMarginSeekBar(iRight, RIGHT, margins, name)
            initMarginSeekBar(iTop, TOP, margins, name)
            initMarginSeekBar(iBottom, BOTTOM, margins, name)
        }
    }

    private enum class Direction {
        LEFT, RIGHT, TOP, BOTTOM
    }

    private fun initMarginSeekBar(layout: View, direction: Direction, margins: Margins, name: ReaderConfigName) {
        // 进度条从-1开始，
        val minValue = -1
        val nameTextView: TextView = layout.tvMarginName
        val decreaseTextView: TextView = layout.tvDecrease
        val seekBar: SeekBar = layout.sbMargin
        val increaseTextView: TextView = layout.tvIncrease
        val valueTextView: TextView = layout.tvMarginValue
        fun getValue() = when (direction) {
            LEFT -> margins.left
            RIGHT -> margins.right
            TOP -> margins.top
            BOTTOM -> margins.bottom
        }

        fun setValue(value: Int) = when (direction) {
            LEFT -> margins.left = value
            RIGHT -> margins.right = value
            TOP -> margins.top = value
            BOTTOM -> margins.bottom = value
        }
        nameTextView.text = view.getString(R.string.margin_name_placeholder, view.getString(when (direction) {
            LEFT -> R.string.left
            RIGHT -> R.string.right
            TOP -> R.string.top
            BOTTOM -> R.string.bottom
        }))
        valueTextView.text = view.getString(R.string.margin_value_placeholder, getValue())
        decreaseTextView.setOnClickListener {
            val value = seekBar.progress + minValue - 1
            seekBar.progress = value - minValue
            setValue(value)
            view.setMargins(margins, name)
            valueTextView.text = view.getString(R.string.margin_value_placeholder, value)

        }
        increaseTextView.setOnClickListener {
            val value = seekBar.progress + minValue + 1
            seekBar.progress = value - minValue
            setValue(value)
            view.setMargins(margins, name)
            valueTextView.text = view.getString(R.string.margin_value_placeholder, value)
        }
        seekBar.progress = getValue() + 1
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (!fromUser) {
                    return
                }
                val value = progress + minValue
                setValue(value)
                view.setMargins(margins, name)
                valueTextView.text = view.getString(R.string.margin_value_placeholder, value)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                val value = seekBar.progress + minValue
                setValue(value)
            }
        })
    }

    fun reset(currentTextCount: Int, currentTextProgress: Int) {
        showLayout(mPanelDefault)

        mPanelDefault.sbTextProgress.apply {
            max = currentTextCount
            progress = currentTextProgress
        }
    }
}