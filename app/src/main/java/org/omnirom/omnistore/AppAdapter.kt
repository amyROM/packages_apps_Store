package org.omnirom.omnistore

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso


class AppAdapter(val items: ArrayList<AppItem>, val context: Context) :
    RecyclerView.Adapter<AppAdapter.ViewHolder>() {

    inner class ViewHolder : RecyclerView.ViewHolder {
        lateinit var app: AppItem
        var title: TextView
        var logo: ImageView
        var status: TextView
        var image: ImageView
        var pkg: TextView
        var progress: ProgressBar
        var note: ImageView
        var indicator: ImageView

        constructor(view: View) : super(view) {
            title = view.findViewById(R.id.app_name)
            logo = view.findViewById(R.id.app_logo)
            status = view.findViewById(R.id.app_status)
            image = view.findViewById(R.id.app_image)
            pkg = view.findViewById(R.id.app_pkg)
            progress = view.findViewById(R.id.app_progress)
            note = view.findViewById(R.id.app_note)
            indicator = view.findViewById(R.id.app_indicator)
            view.setOnClickListener {
                if (app.appSettingsEnabled()) {
                    val intent =
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    intent.data = Uri.parse("package:" + app.pkg())
                    it?.context?.startActivity(intent)
                }
            }
            note.setOnClickListener {
                val builder = AlertDialog.Builder(it?.context)
                builder.setTitle(app.title())
                builder.setMessage(app.note());
                builder.setPositiveButton(android.R.string.ok, null)
                builder.create().show()
            }

            image.visibility = View.VISIBLE
            image.setOnClickListener(View.OnClickListener {
                if (app.mDownloadId == -1L) {
                    (context as MainActivity).downloadApp(app)
                } else {
                    (context as MainActivity).cancelDownloadApp(app)
                }
            })
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(context).inflate(R.layout.app_list_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val app: AppItem = items[position]
        holder.app = app
        holder.title.text = app.title()
        if (app.iconUrl() != null) {
            Picasso.with(context).load(app.iconUrl())
                .error(R.mipmap.ic_launcher).into(holder.logo)
        } else {
            holder.logo.setImageResource(R.mipmap.ic_launcher)
        }
        holder.pkg.text = app.pkg()
        holder.indicator.visibility = View.GONE
        holder.note.visibility = View.GONE

        if (app.mDownloadId != -1L) {
            holder.image.setImageResource(R.drawable.ic_cancel)
            holder.image.visibility = View.VISIBLE
            holder.status.text = context.getString(R.string.status_installing)
            holder.progress.visibility = View.VISIBLE
        } else {
            if (app.installEnabled()) {
                holder.image.setImageResource(R.drawable.ic_download)
                holder.image.visibility = View.VISIBLE
            } else {
                holder.image.visibility = View.GONE
            }
            holder.progress.visibility = View.GONE
            if (app.updateAvailable()) {
                holder.status.text =
                    context.getString(R.string.status_update_available) + " " + app.versionName()
                holder.indicator.visibility = View.VISIBLE
            } else if (app.appInstalled()) {
                holder.status.text =
                    context.getString(R.string.status_installed) + " " + app.mVersionName
            } else if (app.appNotInstaled()) {
                holder.status.text =
                    context.getString(R.string.status_not_installed) + " " + app.versionName()
            } else if (app.appDisabled()) {
                holder.status.text =
                    context.getString(R.string.status_disabled) + " " + app.mVersionName
            }
        }

        if (app.note() != null) {
            holder.note.visibility = View.VISIBLE
        }
    }
}