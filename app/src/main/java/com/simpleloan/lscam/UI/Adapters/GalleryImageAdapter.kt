package com.simpleloan.lscam.UI.Adapters

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.simpleloan.lscam.Data.ImageWrapper
import com.simpleloan.lscam.Others.RCVClickListener
import com.simpleloan.lscam.R
import com.simpleloan.lscam.Utils
import kotlinx.android.synthetic.main.gallery_image_view.view.*
import java.util.*


class GalleryImageAdapter constructor(var screenWidth :Int): RecyclerView.Adapter<GalleryImageAdapter.viewHolder>() {

    lateinit var context: Context
    lateinit var listOfImages: MutableList<ImageWrapper>
     var rcvClickListener: RCVClickListener?=null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): viewHolder {
        context = parent.context
        var view = LayoutInflater.from(context).inflate(R.layout.gallery_image_view, parent, false)
        return viewHolder(view)
    }

    override fun getItemCount(): Int {
        return if(::listOfImages.isInitialized) listOfImages.size else 0
    }

    fun updateImagesList(listOfImage :MutableList<ImageWrapper>) {
        this.listOfImages=listOfImage
        Collections.reverse(listOfImages)
        notifyDataSetChanged()
    }

    fun setRcvClicketListener(listener:RCVClickListener){
        this.rcvClickListener=listener
    }

    override fun onBindViewHolder(holder: viewHolder, position: Int) {
        var imageWrapper = listOfImages.get(position)

        holder.image.setLayoutParams(ConstraintLayout.LayoutParams(screenWidth,Utils.dpToPx(context,150)))

        holder.image.setOnClickListener {
            rcvClickListener?.OnItemClick(it, position)
        }

        Glide.with(context).load(imageWrapper.imageUrl).into(holder.image)

    }


    class viewHolder(view: View) : RecyclerView.ViewHolder(view) {

        var image = view.iv_gallery_image

    }
}