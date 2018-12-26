package com.simpleloan.lscam.UI.Activities

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.facebook.*
import com.facebook.AccessToken
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInResult
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.ResultCallback
import com.simpleloan.lscam.R
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONException
import java.util.*


class MainActivity : AppCompatActivity(), GoogleApiClient.OnConnectionFailedListener {


    val RC_SIGN_IN = 100

    lateinit var context: Context

    lateinit var callbackManager: CallbackManager

    var mProgressDialog: ProgressDialog?=null

    lateinit var  mGoogleApiClient:GoogleApiClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        context = this

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()

           mGoogleApiClient = GoogleApiClient.Builder(this)
            .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
            .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
            .build()


        bt_sign_in_with_google.setOnClickListener {
            val signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient)
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }


        val loggedOut = AccessToken.getCurrentAccessToken() == null

        if (!loggedOut) {
            //Picasso.with(this).load(Profile.getCurrentProfile().getProfilePictureUri(200, 200)).into(imageView)
            Log.d("TAG", "Username is: " + Profile.getCurrentProfile().getName())

            //Using Graph API
            getUserProfile(AccessToken.getCurrentAccessToken())
        }

        login_button.setReadPermissions(Arrays.asList("email", "public_profile"))
        callbackManager = CallbackManager.Factory.create()

        login_button.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                // App code
                //loginResult.getAccessToken();
                //loginResult.getRecentlyDeniedPermissions()
                //loginResult.getRecentlyGrantedPermissions()
                val loggedIn = AccessToken.getCurrentAccessToken() == null
                Log.d("API123", loggedIn.toString() + " ??")
                getUserProfile(AccessToken.getCurrentAccessToken())

            }

            override fun onCancel() {
                // App code
            }

            override fun onError(exception: FacebookException) {
                // App code
            }
        })

    }


    override fun onConnectionFailed(p0: ConnectionResult) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        callbackManager.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {

            if (requestCode == RC_SIGN_IN) {
                val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
                handleSignInResult(result)
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }


    override fun onStart() {
        super.onStart()
        val opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient)
        if (opr.isDone) {
            //Log.d(FragmentActivity.TAG, "Got cached sign-in")
            val result = opr.get()
            handleSignInResult(result)
        } else {

            showProgressDialog()
            opr.setResultCallback(object : ResultCallback<GoogleSignInResult> {

                override fun onResult(googleSignInResult: GoogleSignInResult) {
                    hideProgressDialog()
                    handleSignInResult(googleSignInResult)
                }
            })

        }
    }


    private fun showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = ProgressDialog(this)
            mProgressDialog?.setMessage("Progressing..")
            mProgressDialog?.setIndeterminate(true)
        }

        mProgressDialog?.show()
    }

    private fun hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog!!.isShowing()) {
            mProgressDialog?.hide()
        }

    }


    private fun handleSignInResult(result: GoogleSignInResult) {
        Log.d("az", "handleSignInResult:" + result.isSuccess)
        if (result.isSuccess) {
            val acct = result.signInAccount
            val name = acct?.displayName
            val email = acct?.email
            val photoUri = acct?.photoUrl

            //Toast.makeText(context,name,Toast.LENGTH_LONG).show()

            var bundle = Bundle()
            bundle.putString("name", name)
            bundle.putString("email", email)
            bundle.putString("url", photoUri.toString())

            val intent = Intent(context, DashboardActivity::class.java)
            intent.putExtras(bundle)
            startActivity(intent)

        } else {

        }
    }


    private fun getUserProfile(currentAccessToken: AccessToken) {
        val request = GraphRequest.newMeRequest(
            currentAccessToken
        ) { `object`, response ->
            Log.d("TAG", `object`.toString())
            try {
                val first_name = `object`.getString("first_name")
                val last_name = `object`.getString("last_name")
                val email = `object`.getString("email")
                val id = `object`.getString("id")
                val image_url = "https://graph.facebook.com/$id/picture?type=normal"

                var bundle = Bundle()
                bundle.putString("name", first_name + " " + last_name)
                bundle.putString("email", email)
                bundle.putString("url", image_url.toString())

                val intent = Intent(context, DashboardActivity::class.java)
                intent.putExtras(bundle)
                startActivity(intent)

            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }

        val parameters = Bundle()
        parameters.putString("fields", "first_name,last_name,email,id")
        request.parameters = parameters
        request.executeAsync()
    }



}
