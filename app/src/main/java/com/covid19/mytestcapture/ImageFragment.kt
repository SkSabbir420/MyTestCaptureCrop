package com.covid19.mytestcapture

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.covid19.mytestcapture.databinding.FragmentImageBinding

//import butterknife.BindView
//import butterknife.ButterKnife

class ImageFragment : Fragment() {
    private var bitmap: Bitmap? = null

    //@BindView(R.id.res_photo)
    //var resPhoto: ImageView? = null

    //@BindView(R.id.res_photo_size)
    //var resPhotoSize: TextView? = null
    private lateinit var fragmentBindingImage:FragmentImageBinding

    fun imageSetupFragment(bitmap: Bitmap?) {
        if (bitmap != null) {
            this.bitmap = bitmap
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.setRetainInstance(true)
        fragmentBindingImage = FragmentImageBinding.inflate(layoutInflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_image, container, false)
        //ButterKnife.bind(this, view)
        //check if bitmap exist, set to ImageView
        val imageView = view.findViewById<ImageView>(R.id.res_photo)
        val imagePhotoSize = view.findViewById<TextView>(R.id.res_photo_size)
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap)
            val info = """
                image with:${bitmap!!.width}
                image height:${bitmap!!.height}
                """.trimIndent()
            imagePhotoSize.text = info

        }
        val goToResultActivity = view.findViewById<Button>(R.id.btn_go_result_activity)
        goToResultActivity.setOnClickListener {
            if (bitmap!= null) {
                val resultActivity = ResultActivity()
                resultActivity.setBitMap(bitmap)
                val intent = Intent(context,resultActivity::class.java)
                startActivity(intent)
            }
        }

        return view
    }
}