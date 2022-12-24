package com.covid19.mytestcapture

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.hardware.Camera
import android.hardware.Camera.PictureCallback
import android.hardware.Camera.ShutterCallback
import android.media.MediaScannerConnection
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.covid19.mytestcapture.databinding.FragmentPhotoBinding
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*


class PhotoFragment() : Fragment(), SurfaceHolder.Callback {
    var camera: Camera? = null
    var surfaceView: SurfaceView? = null
    var surfaceHolder: SurfaceHolder? = null
    var previewing = false

   private lateinit var fragmentBinding:FragmentPhotoBinding


    //@BindView(R.id.preview_layout)
    var previewLayout: LinearLayout? = null

    //@BindView(R.id.border_camera)
    var borderCamera: View? = null

    //    @BindView(R.id.res_border_size)
    //    TextView resBorderSizeTV;
    private var mListener: com.covid19.mytestcapture.PhotoFragment.OnFragmentInteractionListener? =
        null
    var previewSizeOptimal: Camera.Size? = null

    interface OnFragmentInteractionListener {
        fun onFragmentInteraction(bitmap: Bitmap?)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.setRetainInstance(true)
        Log.d("MainActivityD","Start OnCreate1")

        fragmentBinding = FragmentPhotoBinding.inflate(layoutInflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d("MainActivityD","Start onCreateview2")
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_photo, container, false)
        //ButterKnife.bind(this, view)
        surfaceView = view.findViewById<View>(R.id.camera_preview_surface) as SurfaceView
        surfaceHolder = surfaceView!!.holder
        surfaceHolder!!.addCallback(this)
        surfaceHolder!!.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)

        val makephoto = view.findViewById<Button>(R.id.make_photo_button)
        previewLayout = view.findViewById(R.id.preview_layout)
        borderCamera = view.findViewById(R.id.border_camera)

        makephoto.setOnClickListener {
            Log.d("MainActivityD","Call makephotobutton")
            if (camera != null) {
                camera!!.takePicture(
                    myShutterCallback,
                    myPictureCallback_RAW, myPictureCallback_JPG
                )
            }
        }

        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is com.covid19.mytestcapture.PhotoFragment.OnFragmentInteractionListener) {
            mListener =
                context as com.covid19.mytestcapture.PhotoFragment.OnFragmentInteractionListener
        } else {
            throw RuntimeException(
                context.toString()
                        + " must implement OnFragmentInteractionListener"
            )
        }
    }


    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        camera = Camera.open()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        if (previewing) {
            camera!!.stopPreview()
            previewing = false
        }
        if (camera != null) {
            try {
                val parameters = camera!!.parameters
                //get preview sizes
                val previewSizes = parameters.supportedPreviewSizes

                //find optimal - it very important
                previewSizeOptimal = getOptimalPreviewSize(
                    previewSizes, parameters.pictureSize.width,
                    parameters.pictureSize.height
                )

                //set parameters
                if (previewSizeOptimal != null) {
                    parameters.setPreviewSize(
                        previewSizeOptimal!!.width,
                        previewSizeOptimal!!.height
                    )
                }
                if (camera!!.parameters.focusMode.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                    parameters.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE
                }
                if (camera!!.parameters.flashMode.contains(Camera.Parameters.FLASH_MODE_AUTO)) {
                    parameters.flashMode = Camera.Parameters.FLASH_MODE_AUTO
                }
                camera!!.parameters = parameters

                //rotate screen, because camera sensor usually in landscape mode
                val display =
                    (requireContext().getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
                if (display.rotation == Surface.ROTATION_0) {
                    camera!!.setDisplayOrientation(90)
                } else if (display.rotation == Surface.ROTATION_270) {
                    camera!!.setDisplayOrientation(180)
                }

                //write some info
                val x1 = previewLayout!!.width
                val y1 = previewLayout!!.height
                val x2 = borderCamera!!.width
                val y2 = borderCamera!!.height
                val info =
                    ("Preview width:" + x1.toString() + "\n" + "Preview height:" + y1.toString() + "\n" +
                            "Border width:" + x2.toString() + "\n" + "Border height:" + y2.toString())
                //resBorderSizeTV.setText(info);
                camera!!.setPreviewDisplay(surfaceHolder)
                camera!!.startPreview()
                previewing = true
            } catch (e: IOException) {
                // TODO Auto-generated catch block
                e.printStackTrace()
            }
        }
    }

    fun getOptimalPreviewSize(sizes: List<Camera.Size>?, w: Int, h: Int): Camera.Size? {
        val ASPECT_TOLERANCE = 0.1
        val targetRatio = w.toDouble() / h
        if (sizes == null) return null
        var optimalSize: Camera.Size? = null
        var minDiff = Double.MAX_VALUE
        val targetHeight = h

        // Try to find an size match aspect ratio and size
        for (size: Camera.Size in sizes) {
            val ratio = size.width.toDouble() / size.height
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size
                minDiff = Math.abs(size.height - targetHeight).toDouble()
            }
        }

        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE
            for (size: Camera.Size in sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size
                    minDiff = Math.abs(size.height - targetHeight).toDouble()
                }
            }
        }
        return optimalSize
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        camera!!.stopPreview()
        camera!!.release()
        camera = null
        previewing = false
    }

//    @OnClick(R.id.make_photo_button)
//    fun makePhoto() {
//        if (camera != null) {
//            camera!!.takePicture(
//                myShutterCallback,
//                myPictureCallback_RAW, myPictureCallback_JPG
//            )
//        }
//    }


    var myShutterCallback: ShutterCallback = ShutterCallback { }
    var myPictureCallback_RAW: PictureCallback =
        PictureCallback { data, camera -> }
    var myPictureCallback_JPG: PictureCallback =
        PictureCallback { data, camera ->
            val bitmapPicture = BitmapFactory.decodeByteArray(data, 0, data.size)
            var croppedBitmap: Bitmap? = null
            val display =
                (requireContext().getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
            if (display.rotation == Surface.ROTATION_0) {

                //rotate bitmap, because camera sensor usually in landscape mode
                val matrix = Matrix()
                matrix.postRotate(90f)
                val rotatedBitmap = Bitmap.createBitmap(
                    bitmapPicture,
                    0,
                    0,
                    bitmapPicture.width,
                    bitmapPicture.height,
                    matrix,
                    true
                )
                //save file
               /// createImageFile(rotatedBitmap)

                //calculate aspect ratio
                val koefX = rotatedBitmap.width.toFloat() / previewLayout!!.width.toFloat()
                val koefY = rotatedBitmap.height.toFloat() / previewLayout!!.height.toFloat()

                //get viewfinder border size and position on the screen
                val x1 = borderCamera!!.left
                val y1 = borderCamera!!.top
                val x2 = borderCamera!!.width
                val y2 = borderCamera!!.height

                //calculate position and size for cropping
                val cropStartX = Math.round(x1 * koefX)
                val cropStartY = Math.round(y1 * koefY)
                val cropWidthX = Math.round(x2 * koefX)
                val cropHeightY = Math.round(y2 * koefY)

                //check limits and make crop
                if (cropStartX + cropWidthX <= rotatedBitmap.width && cropStartY + cropHeightY <= rotatedBitmap.height) {
                    croppedBitmap = Bitmap.createBitmap(
                        rotatedBitmap,
                        cropStartX,
                        cropStartY,
                        cropWidthX,
                        cropHeightY
                    )
                } else {
                    croppedBitmap = null
                }

                //save result
                croppedBitmap?.let { createImageFile(it) }
            } else if (display.rotation == Surface.ROTATION_270) {
                // for Landscape mode
            }

            //pass to another fragment
            if (mListener != null) {
                if (croppedBitmap != null) mListener!!.onFragmentInteraction(croppedBitmap)
            }
            if (camera != null) {
                camera.startPreview()
            }
        }

    fun createImageFile(bitmap: Bitmap) {
        val path = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES
        )
        //val timeStamp = SimpleDateFormat("MMdd_HHmmssSSS").format(Date())

        //val imageFileName = "region_$timeStamp.jpg"
        val imageFileName = "region_.jpg"
        val file = File(path, imageFileName)
        try {
            // Make sure the Pictures directory exists.
            if (path.mkdirs()) {
                Toast.makeText(context, "Not exist :" + path.name, Toast.LENGTH_SHORT).show()
            }
            val os: OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os)
            os.flush()
            os.close()
            Log.i("ExternalStorage", "Writed " + path + file.name)
            // Tell the media scanner about the new file so that it is
            // immediately available to the user.
            MediaScannerConnection.scanFile(
                context, arrayOf(file.toString()), null
            ) { path, uri ->
                Log.i("ExternalStorage", "Scanned $path:")
                Log.i("ExternalStorage", "-> uri=$uri")
            }
            Toast.makeText(context, file.name, Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            // Unable to create file, likely because external storage is
            // not currently mounted.
            Log.w("ExternalStorage", "Error writing $file", e)
        }
    }
}