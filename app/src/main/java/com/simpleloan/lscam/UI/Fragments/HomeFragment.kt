package com.simpleloan.lscam.UI.Fragments


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.facebook.login.LoginManager
import com.simpleloan.lscam.Others.SignOutListener
import com.simpleloan.lscam.R
import com.simpleloan.lscam.UI.Activities.MainActivity
import de.hdodenhof.circleimageview.CircleImageView



class HomeFragment : Fragment() {

    lateinit var mContext:Context
    lateinit var mActivity:Activity

    lateinit var mSignOutListener: SignOutListener

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        view.findViewById<Button>(R.id.bt_camera).setOnClickListener {
            val navController = Navigation.findNavController(view)
            val action = HomeFragmentDirections.action_home_to_camera()
            var email=activity?.intent?.extras?.getString("email")
            email=email?.replace(".","")
            action.setEmail(email)
            navController.navigate(action)

        }


        view.findViewById<Button>(R.id.bt_galllery).setOnClickListener {
            val navController = Navigation.findNavController(view)
            val action = HomeFragmentDirections.action_home_to_gallery()
            var email=activity?.intent?.extras?.getString("email")
            email=email?.replace(".","")
            action.setEmail(email)
            navController.navigate(action)

        }

        return view
    }



    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        mContext=context!!
        mActivity=activity!!

        var activityCompat = activity as AppCompatActivity
        activityCompat.supportActionBar?.setDisplayShowTitleEnabled(true)
        activityCompat.supportActionBar?.title="LSCam"
        activityCompat.supportActionBar?.setBackgroundDrawable(ColorDrawable(Color.parseColor("#146eb4")))


        mSignOutListener = activity as SignOutListener

        var bundle = activity?.intent?.extras

        view?.findViewById<TextView>(R.id.tv_name)?.setText(bundle?.getString("name"))
        view?.findViewById<TextView>(R.id.tv_email)?.setText(bundle?.getString("email"))

        Glide.with(activity!!)
            .load(bundle?.getString("url"))
            .into(view!!.findViewById<CircleImageView>(R.id.iv_profile))

        view?.findViewById<Button>(R.id.bt_logout)?.setOnClickListener {
            LoginManager.getInstance().logOut()
            mSignOutListener.OnSignOutClick()

            startActivity(Intent(mContext,MainActivity::class.java))
        }


    }





}
