package com.simpleloan.lscam.UI.Fragments

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.simpleloan.lscam.R


class FullImageFragment : Fragment() {

    lateinit var imageUrl: String
    lateinit var bundle: Bundle;

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_full_image, container, false)
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

         bundle = arguments!!
        imageUrl = bundle.getString("ImageUrl")

        var activityCompat = activity as AppCompatActivity
        activityCompat.supportActionBar?.setDisplayShowTitleEnabled(true)
        activityCompat.supportActionBar?.title="Gallery"
        activityCompat.supportActionBar?.setBackgroundDrawable(ColorDrawable(Color.parseColor("#146eb4")))


        var mImage = view!!.findViewById<ImageView>(R.id.iv_image) as ImageView
        Glide.with(context!!).load(imageUrl).into(mImage)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater?.inflate(R.menu.menu_image_details, menu)
    }


    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        when (item?.itemId) {
            R.id.menu_details -> {
                //Toast.makeText(context, "Clicked", Toast.LENGTH_LONG).show()
                showDialog()
            }
        }
        return super.onOptionsItemSelected(item)
    }


    fun showDialog() {

        var dialog = Dialog(context,R.style.Theme_Dialog)
        dialog.setContentView(R.layout.custom_dialog);
        dialog.setTitle("Details")

        var imageName = dialog.findViewById<TextView>(R.id.tv_image_name) as TextView
        var imageLongitude = dialog.findViewById<TextView>(R.id.tv_longitude) as TextView
        var imageLatitude = dialog.findViewById<TextView>(R.id.tv_latitude) as TextView
        var close = dialog.findViewById<Button>(R.id.bt_close) as Button

        imageName.setText("Name :"+bundle.getString("ImageName"))
        imageLongitude.setText("Longitude :"+bundle.getString("Longitude"))
        imageLatitude.setText("Latitude :"+bundle.getString("Latitude"))

        close.setOnClickListener { dialog.dismiss() }

        dialog.show()


    }
}
