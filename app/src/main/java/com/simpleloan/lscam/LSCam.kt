package com.simpleloan.lscam

import android.app.Application
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger
import com.google.firebase.FirebaseApp

class LSCam : Application(){

    companion object {
        lateinit var app :LSCam

        fun getInstance(): LSCam{
            return  app
        }
    }


    override fun onCreate() {
        super.onCreate()
        app=this
        FirebaseApp.initializeApp(this)

        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
    }


}