package com.simpleloan.lscam.UI.Fragments

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.simpleloan.lscam.Data.ImageWrapper
import com.simpleloan.lscam.Others.RCVClickListener
import com.simpleloan.lscam.R
import com.simpleloan.lscam.UI.Adapters.GalleryImageAdapter
import com.simpleloan.lscam.Utils


class GalleryFragment : Fragment(), RCVClickListener {


    lateinit var mEmail: String

    lateinit var mAdapter: GalleryImageAdapter
    var mListOfUrls = mutableListOf<ImageWrapper>()
    var mapOfMetaData = mutableMapOf<String, StorageMetadata>()

    lateinit var mContext: Context

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {

        mEmail = GalleryFragmentArgs.fromBundle(arguments).email
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_gallery, container, false)
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        mContext = context!!

        var activityCompat = activity as AppCompatActivity
        activityCompat.supportActionBar?.setDisplayShowTitleEnabled(true)
        activityCompat.supportActionBar?.title="Gallery"
        activityCompat.supportActionBar?.setBackgroundDrawable(ColorDrawable(Color.parseColor("#146eb4")))


        var rcvImages = view!!.findViewById<RecyclerView>(R.id.rv_gallery_images) as RecyclerView
        rcvImages.layoutManager = GridLayoutManager(context, 3, LinearLayoutManager.VERTICAL, false)

        mAdapter = GalleryImageAdapter((Utils.getScreenWidth(mContext) / 3))
        mAdapter.setRcvClicketListener(this)
        rcvImages.adapter = mAdapter




    }


    override fun onResume() {
        super.onResume()

        mListOfUrls.clear()

        var dataBaseRef = FirebaseDatabase.getInstance().getReference("Users")

        dataBaseRef.child(mEmail).addChildEventListener(object : ChildEventListener {
            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
                Log.d("az", "image :" + p0.key)
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                Log.d("az", "image :" + p0.key)
            }

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                Log.d("az", "added image :" + p0.key)
                mListOfUrls.add(ImageWrapper(p0.key as String, p0.value as String))
                mAdapter.updateImagesList(mListOfUrls)

                getMetaData(p0.key.toString())
            }

            override fun onChildRemoved(p0: DataSnapshot) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

        })

    }

    fun getMetaData(imageName: String) {

        var storageReference = FirebaseStorage.getInstance().getReferenceFromUrl("gs://lscam-bf7e0.appspot.com/")
        val forestRef = storageReference.child(imageName + ".jpg")
        forestRef.metadata.addOnSuccessListener {
            mapOfMetaData.put(it.name.toString(), it)
        }.addOnFailureListener {
            // Uh-oh, an error occurred!
        }
    }

    override fun OnItemClick(clickView: View, position: Int) {
        var imageName = mListOfUrls.get(position).imageName + ".jpg"

        var storageMetadata = mapOfMetaData.get(imageName)

        var bundle = Bundle()
        bundle.putString("ImageUrl",mListOfUrls.get(position).imageUrl)
        bundle.putString("ImageName",imageName)
        bundle.putString("Latitude",storageMetadata?.getCustomMetadata("Latitude"))
        bundle.putString("Longitude",storageMetadata?.getCustomMetadata("Longitude"))

        val navController = Navigation.findNavController(view as View)
        navController.navigate(R.id.action_gallery_to_full_image,bundle)

    }
}
