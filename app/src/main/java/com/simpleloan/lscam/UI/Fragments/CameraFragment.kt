package com.simpleloan.lscam.UI.Fragments


import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.support.annotation.StringRes
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.ActivityCompat
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.Toast
import androidx.navigation.Navigation
import com.google.android.cameraview.AspectRatio
import com.google.android.cameraview.CameraView
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.UploadTask
import com.simpleloan.lscam.Others.GPSTracker
import com.simpleloan.lscam.R
import com.simpleloan.lscam.Utils


class CameraFragment : Fragment(), ActivityCompat.OnRequestPermissionsResultCallback,
    AspectRatioFragment.Listener {

    private val TAG = "MainActivity"

    private val REQUEST_CAMERA_PERMISSION = 1

    private val FRAGMENT_DIALOG = "dialog"


    private val FLASH_OPTIONS = intArrayOf(CameraView.FLASH_AUTO, CameraView.FLASH_OFF, CameraView.FLASH_ON)

    private val FLASH_ICONS = intArrayOf(R.drawable.ic_flash_auto, R.drawable.ic_flash_off, R.drawable.ic_flash_on)

    private val FLASH_TITLES = intArrayOf(R.string.flash_auto, R.string.flash_off, R.string.flash_on)

    private var mCurrentFlash: Int = 0

    private lateinit var mCameraView: CameraView

    private lateinit var mEmail: String

    private lateinit var mCapturedImage: ImageView

    private var mBackgroundHandler: Handler? = null


    private val mOnClickListener = View.OnClickListener { v ->
        when (v.id) {
            R.id.take_picture -> if (mCameraView != null) {
                mCameraView!!.takePicture()
            }
        }
    }


    lateinit var mContext: Context
    lateinit var mActivity: Activity
    private var mGPSTracker = GPSTracker()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        setHasOptionsMenu(true)
        val view = inflater.inflate(R.layout.fragment_camera, container, false)

        mEmail = CameraFragmentArgs.fromBundle(arguments).email

//        view.findViewById<Button>(R.id.bt_back_to_home).setOnClickListener {
//            val navController = Navigation.findNavController(view)
//            navController.navigateUp()
//        }


        return view
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mCameraView = view!!.findViewById<CameraView>(R.id.camera) as CameraView

        mCapturedImage = view!!.findViewById<ImageView>(R.id.iv_captured_image) as ImageView
        mCapturedImage.visibility = View.GONE

        var activityCompat = activity as AppCompatActivity
        activityCompat.supportActionBar?.setDisplayShowTitleEnabled(false)
        activityCompat.supportActionBar?.setBackgroundDrawable(ColorDrawable(Color.parseColor("#00000000")))

        mCapturedImage.setOnClickListener {
            val navController = Navigation.findNavController(view as View)
            var action = CameraFragmentDirections.action_camera_to_gallery()
            action.setEmail(mEmail)
            navController.navigate(action)
        }

        mCameraView.addCallback(mCallback)

        val fab = view?.findViewById<FloatingActionButton>(R.id.take_picture) as FloatingActionButton
        fab.setOnClickListener(mOnClickListener)

        mContext = context!!
        mActivity = activity!!
    }


    @SuppressLint("MissingPermission")
    override fun onResume() {
        super.onResume()



        mActivity.startService(Intent(mActivity, GPSTracker::class.java))

        if ((ContextCompat.checkSelfPermission(
                mContext,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED)
        ) {
            mCameraView.start()
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(mActivity, Manifest.permission.CAMERA)) {
            ConfirmationDialogFragment.newInstance(
                R.string.camera_permission_confirmation,
                arrayOf(Manifest.permission.CAMERA),
                REQUEST_CAMERA_PERMISSION,
                R.string.camera_permission_not_granted
            ).show(getFragmentManager(), FRAGMENT_DIALOG)
        } else {
            ActivityCompat.requestPermissions(mActivity, arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
        }
    }

    override fun onPause() {
        mCameraView.stop()
        super.onPause()
    }


    override fun onStop() {
        super.onStop()
        mActivity.stopService(Intent(mActivity, GPSTracker::class.java))
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mBackgroundHandler != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                mBackgroundHandler!!.getLooper().quitSafely()
            } else {
                mBackgroundHandler!!.getLooper().quit()
            }
            mBackgroundHandler = null
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_CAMERA_PERMISSION -> {
                if (permissions.size != 1 || grantResults.size != 1) {
                    throw RuntimeException("Error on requesting camera permission.")
                }
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(mContext, R.string.camera_permission_not_granted, Toast.LENGTH_SHORT).show()
                }
            }
        }// No need to start camera here; it is handled by onResume
    }


    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        // (activity as AppCompatActivity?)?.getSupportActionBar()?.

        inflater?.inflate(R.menu.main, menu)
        super.onCreateOptionsMenu(menu, inflater)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.aspect_ratio -> {
                val fragmentManager = getFragmentManager()
                if ((mCameraView != null && fragmentManager?.findFragmentByTag(FRAGMENT_DIALOG) == null)) {
                    val ratios = mCameraView.getSupportedAspectRatios()
                    val currentRatio = mCameraView.getAspectRatio()
                    AspectRatioFragment.newInstance(ratios, currentRatio)
                        .show(fragmentManager, FRAGMENT_DIALOG)
                }
                return true
            }
            R.id.switch_flash -> {
                if (mCameraView != null) {
                    mCurrentFlash = (mCurrentFlash + 1) % FLASH_OPTIONS.size
                    item.setTitle(FLASH_TITLES[mCurrentFlash])
                    item.setIcon(FLASH_ICONS[mCurrentFlash])
                    mCameraView.setFlash(FLASH_OPTIONS[mCurrentFlash])
                }
                return true
            }
            R.id.switch_camera -> {
                if (mCameraView != null) {
                    val facing = mCameraView.getFacing()
                    mCameraView.setFacing(
                        if (facing == CameraView.FACING_FRONT)
                            CameraView.FACING_BACK
                        else
                            CameraView.FACING_FRONT
                    )
                }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onAspectRatioSelected(ratio: AspectRatio) {
        if (mCameraView != null) {
            Toast.makeText(mContext, ratio.toString(), Toast.LENGTH_SHORT).show()
            mCameraView.setAspectRatio(ratio)
        }
    }

    private fun getBackgroundHandler(): Handler {
        if (mBackgroundHandler == null) {
            val thread = HandlerThread("background")
            thread.start()
            mBackgroundHandler = Handler(thread.looper)
        }

        return mBackgroundHandler as Handler
    }

    private val mCallback = object : CameraView.Callback() {

        override fun onCameraOpened(cameraView: CameraView?) {
            Log.d(TAG, "onCameraOpened")
        }

        override fun onCameraClosed(cameraView: CameraView?) {
            Log.d(TAG, "onCameraClosed")
        }

        override fun onPictureTaken(cameraView: CameraView, data: ByteArray) {
            Log.d(TAG, "onPictureTaken " + data.size)
            Toast.makeText(cameraView.context, R.string.picture_taken, Toast.LENGTH_SHORT).show()
            var storageReference = FirebaseStorage.getInstance().getReferenceFromUrl("gs://lscam-bf7e0.appspot.com/")

            var bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
            mCapturedImage.visibility = View.VISIBLE
            mCapturedImage.setImageBitmap(bitmap)

            val imageName = "IMG_" + mEmail + "_" + Utils.timestamp
            var fileRef = storageReference.child(imageName + ".jpg")

            /* val filePath = Environment.getExternalStorageDirectory().toString() + "/Cam/"+imageName+".jpg"
             var file=File(filePath)
             val out = BufferedOutputStream(FileOutputStream(file))
             bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
             out.flush()
             out.close()
             val exif = ExifInterface(filePath)
             exif.setAttribute("Latitude","22.4677677")
             exif.setAttribute("Longitude","72.46777")
             exif.saveAttributes()*/

            Log.d("az", "Latitude " + mGPSTracker.getLatitude().toString())

            val metadata = StorageMetadata.Builder()
                .setCustomMetadata("Latitude", mGPSTracker.getLatitude().toString())
                .setCustomMetadata("Longitude", mGPSTracker.getLongitude().toString())
                .build()

            val urlTask =
                fileRef.putBytes(data, metadata)
                    .continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                        if (!task.isSuccessful) {
                            task.exception?.let {
                                throw it
                            }
                        }
                        return@Continuation fileRef.downloadUrl
                    }).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val downloadUri = task.result
                        Log.d("az", "image url " + downloadUri)

                        var firebaseDatabase = FirebaseDatabase.getInstance().getReference("Users")
                        var map = mutableMapOf<String, String>()

                        map.put(imageName, downloadUri.toString())
                        firebaseDatabase.child(mEmail).updateChildren(map as Map<String, Any>)

                    } else {
                        Toast.makeText(mContext, "upload failed", Toast.LENGTH_LONG).show()
                    }
                }
        }

    }

    class ConfirmationDialogFragment : DialogFragment() {

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val args = arguments
            return AlertDialog.Builder(activity!!)
                .setMessage(args!!.getInt(ARG_MESSAGE))
                .setPositiveButton(
                    android.R.string.ok
                ) { dialog, which ->
                    val permissions = args!!.getStringArray(ARG_PERMISSIONS) ?: throw IllegalArgumentException()
                    ActivityCompat.requestPermissions(
                        activity!!,
                        permissions!!, args!!.getInt(ARG_REQUEST_CODE)
                    )
                }
                .setNegativeButton(android.R.string.cancel,
                    object : DialogInterface.OnClickListener {
                        override fun onClick(dialog: DialogInterface, which: Int) {
                            Toast.makeText(
                                activity,
                                args!!.getInt(ARG_NOT_GRANTED_MESSAGE),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    })
                .create()
        }

        companion object {

            private val ARG_MESSAGE = "message"
            private val ARG_PERMISSIONS = "permissions"
            private val ARG_REQUEST_CODE = "request_code"
            private val ARG_NOT_GRANTED_MESSAGE = "not_granted_message"

            fun newInstance(
                @StringRes message: Int,
                permissions: Array<String>, requestCode: Int, @StringRes notGrantedMessage: Int
            ): ConfirmationDialogFragment {
                val fragment = ConfirmationDialogFragment()
                val args = Bundle()
                args.putInt(ARG_MESSAGE, message)
                args.putStringArray(ARG_PERMISSIONS, permissions)
                args.putInt(ARG_REQUEST_CODE, requestCode)
                args.putInt(ARG_NOT_GRANTED_MESSAGE, notGrantedMessage)
                fragment.arguments = args
                return fragment
            }
        }

    }





}
