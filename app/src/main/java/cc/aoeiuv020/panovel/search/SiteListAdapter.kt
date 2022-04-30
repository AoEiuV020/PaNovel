package cc.aoeiuv020.panovel.search

import android.graphics.Color
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import cc.aoeiuv020.panovel.R
import cc.aoeiuv020.panovel.data.DataManager
import cc.aoeiuv020.panovel.data.entity.Site
import cc.aoeiuv020.panovel.util.hide
import cc.aoeiuv020.panovel.util.show
import cc.aoeiuv020.panovel.util.tip
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.site_list_item.view.*

/**
 * Created by AoEiuV020 on 2018.05.13-16:50:53.
 */
class SiteListAdapter(
        siteList: List<Site>,
        private val itemListener: ItemListener
) : androidx.recyclerview.widget.RecyclerView.Adapter<SiteListAdapter.ViewHolder>() {
    private val data: MutableList<Site> = siteList.toMutableList()
    override fun getItemCount(): Int {
        return data.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.site_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data[position])
    }

    fun move(from: Int, to: Int) {
        if (from == to || from !in data.indices || to !in data.indices) {
            // 位置不正确就直接返回，
            return
        }
        // ArrayList直接删除插入的话性能不行，但是无所谓了，
        val novel = data.removeAt(from)
        data.add(to, novel)
        notifyItemMoved(from, to)
    }

    inner class ViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
        private val tvStopUpkeep: TextView = itemView.tvStopUpkeep
        private val tvName: TextView = itemView.tvName
        private val ivLogo: ImageView = itemView.ivLogo
        private val tvLogo: TextView = itemView.tvLogo
        private val ivSettings: ImageView = itemView.ivSettings
        val cbEnabled: CheckBox = itemView.cbEnabled
        lateinit var site: Site

        init {
            cbEnabled.setOnCheckedChangeListener { _, checked: Boolean ->
                if (site.enabled == checked) {
                    return@setOnCheckedChangeListener
                }
                site.enabled = checked
                itemListener.onEnabledChanged(site)
            }
            itemView.setOnClickListener {
                itemListener.onSiteSelect(site)
            }
            itemView.setOnLongClickListener {
                itemListener.onItemLongClick(this)
            }
            ivSettings.setOnClickListener {
                itemListener.onSettingsClick(site)
            }
        }

        fun bind(data: Site) {
            this.site = data
            tvName.text = data.name
            val logoUri = Uri.parse(data.logo)
            if (logoUri.scheme == "text") {
                tvLogo.show()
                ivLogo.hide()
                val foregroundColor = logoUri.getQueryParameter("fc") ?: "111111"
                val backgroundColor = logoUri.getQueryParameter("bc") ?: "ffffff"
                tvLogo.setBackgroundColor(Color.parseColor("#$backgroundColor"))
                tvLogo.setTextColor(Color.parseColor("#$foregroundColor"))
                tvLogo.text = logoUri.host
            } else {
                tvLogo.hide()
                ivLogo.show()
                Glide.with(ivLogo).load(data.logo).into(ivLogo)
            }
            cbEnabled.isChecked = data.enabled
            val novelContext = DataManager.api.getNovelContextByName(data.name)
            if (novelContext.upkeep) {
                tvStopUpkeep.hide()
            } else {
                tvStopUpkeep.show()
                tvStopUpkeep.setOnClickListener { v ->
                    v.context.tip(novelContext.reason.takeIf { it.isNotEmpty() }
                        ?: v.context.getString(R.string.tip_stop_upkeep_reason_default))
                }
            }
        }
    }

    interface ItemListener {
        fun onEnabledChanged(site: Site)
        fun onSiteSelect(site: Site)
        fun onItemLongClick(vh: ViewHolder): Boolean
        fun onSettingsClick(site: Site)
    }

}