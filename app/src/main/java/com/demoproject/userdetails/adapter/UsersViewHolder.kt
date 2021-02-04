package com.demoproject.userdetails.adapter

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.demoproject.userdetails.R
import com.demoproject.userdetails.interfaces.ItemClickListener
import de.hdodenhof.circleimageview.CircleImageView

class UsersViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

    var userFullName: TextView = itemView.findViewById(R.id.userFullName)
    var userEmail: TextView = itemView.findViewById(R.id.userEmail)
    var userProfileImage: CircleImageView = itemView.findViewById(R.id.userProfileImage)
    var loadingIndicator: ImageView = itemView.findViewById(R.id.loadingIndicator)

    private var itemClickListener: ItemClickListener? = null
    fun setItemClickListener(itemClickListener: ItemClickListener?) {
        this.itemClickListener = itemClickListener
    }

    override fun onClick(view: View) {
        itemClickListener!!.onClick(view)
    }

    init {
        itemView.setOnClickListener(this)
    }
}