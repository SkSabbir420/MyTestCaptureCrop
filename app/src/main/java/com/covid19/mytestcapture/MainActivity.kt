package com.covid19.mytestcapture

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.covid19.mytestcapture.databinding.ActivityMainBinding

//import butterknife.ButterKnife
//import butterknife.OnClick

//import androidx.appcompat.app.AppCompatActivity
//import android.os.Bundle
//
//class MainActivity : AppCompatActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
//    }
//}



class MainActivity : AppCompatActivity(), PhotoFragment.OnFragmentInteractionListener {
    var PERMISSION_ALL = 1
    var flagPermissions = false
    var PERMISSIONS = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.CAMERA
    )

    private lateinit var activityMainBinding:ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_main)
        //ButterKnife.bind(this)
        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)

        checkPermissions()

        activityMainBinding.makePhotoButton.setOnClickListener {

            Log.d("MainActivityD","Start onClickScanButton")
            // check permissions
            if (!flagPermissions) {
                checkPermissions()
            }
            //start photo fragment
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.res_photo_layout, PhotoFragment())
                .addToBackStack(null)
                .commit()
        }
    }



    private fun checkPermissions() {
        if (!hasPermissions(this, *PERMISSIONS)) {
            requestPermissions(
                PERMISSIONS,
                PERMISSION_ALL
            )
            flagPermissions = false
        }
        flagPermissions = true
    }

    override fun onFragmentInteraction(bitmap: Bitmap?) {
        if (bitmap != null) {
            val imageFragment = ImageFragment()
            imageFragment.imageSetupFragment(bitmap)
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.res_photo_layout, imageFragment)
                .addToBackStack(null)
                .commit()
        }
    }

    companion object {
        fun hasPermissions(context: Context?, vararg permissions: String?): Boolean {
            if (context != null && permissions != null) {
                for (permission in permissions) {
                    if (ActivityCompat.checkSelfPermission(
                            context,
                            permission!!
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        return false
                    }
                }
            }
            return true
        }
    }
}