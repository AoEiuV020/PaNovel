package cc.aoeiuv020.panovel.search

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import cc.aoeiuv020.panovel.R
import cc.aoeiuv020.panovel.api.NovelSite
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.site_list_item.view.*

/**
 * Created by AoEiuV020 on 2018.05.13-16:50:53.
 */
class SiteListAdapter(
        private val novelSiteList: List<NovelSite>,
        private val itemListener: SiteChooseActivity.ItemListener
) : RecyclerView.Adapter<SiteListAdapter.ViewHolder>() {
    override fun getItemCount(): Int {
        return novelSiteList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.site_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(novelSiteList[position])
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.tvName
        val ivLogo: ImageView = itemView.ivLogo
        val cbEnabled: CheckBox = itemView.cbEnabled
        private lateinit var data: NovelSite

        init {
            cbEnabled.setOnCheckedChangeListener { _, checked: Boolean ->
                if (data.enabled == checked) {
                    return@setOnCheckedChangeListener
                }
                data.enabled = checked
                itemListener.onEnabledChanged(data, checked)
            }
            itemView.setOnClickListener {
                itemListener.onSiteSelect(data)
            }
        }

        fun bind(data: NovelSite) {
            this.data = data
            tvName.text = data.name
            Glide.with(ivLogo).load(data.logo).into(ivLogo)
            cbEnabled.isChecked = data.enabled
        }
    }
}