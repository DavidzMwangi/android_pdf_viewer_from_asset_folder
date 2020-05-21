package com.wanjohi.david

import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.Build
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.util.Log
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import java.io.*


class MainActivity : AppCompatActivity() {
    var previousBtn:Button?=null
    var nextBtn:Button?=null
    var imageView:ImageView?=null
    var scaleGestureDetector:ScaleGestureDetector?=null
    private var mScaleFactor = 1.0f
    val FILENAME = "hte330k.pdf"
    var pdfRenderer:PdfRenderer?=null
    var position:Int=0
    //open file in assets
    var fileDescriptor: ParcelFileDescriptor?=null

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        previousBtn=findViewById(R.id.previous_btn)
        nextBtn=findViewById(R.id.next_btn)
        imageView=findViewById(R.id.image_pdf)

        scaleGestureDetector= ScaleGestureDetector(this,object :ScaleGestureDetector.OnScaleGestureListener{
            override fun onScaleBegin(detector: ScaleGestureDetector?): Boolean {
               return true
            }

            override fun onScaleEnd(detector: ScaleGestureDetector?) {

            }

            override fun onScale(detector: ScaleGestureDetector?): Boolean {
                mScaleFactor*= scaleGestureDetector?.scaleFactor!!
                mScaleFactor = Math.max(0.1f,Math.min(mScaleFactor,10.0f))
                imageView?.scaleX=mScaleFactor
                imageView?.scaleY=mScaleFactor
                return true
            }

        })

        // Create file object to read and write on
        val file = File(applicationContext.getCacheDir(), FILENAME)
        if (!file.exists()) {
            val assetManager: AssetManager = applicationContext.getAssets()
            copyAsset(assetManager, FILENAME, file.getAbsolutePath())
        }
        fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)

         pdfRenderer = PdfRenderer(fileDescriptor!!)

        openPDF(position)

        previousBtn?.setOnClickListener{
            if (position>0){
                position-=1
                openPDF(position)
            }
            if(position==0){
                Toast.makeText(applicationContext,"You are at the beginning of the pdf",Toast.LENGTH_SHORT).show()
            }
        }

        nextBtn?.setOnClickListener{
            if (position<(pdfRenderer?.pageCount!!-1)){
                position+=1
                openPDF(position)
            }

            if (position==(pdfRenderer?.pageCount!!-1)){
                Toast.makeText(applicationContext,"You have reached the end of the pdf",Toast.LENGTH_SHORT).show()
            }
        }

    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    @Throws(IOException::class)
    private fun openPDF(page:Int) {

        //Display page
        val rendererPage = pdfRenderer?.openPage(page)
        val rendererPageWidth = rendererPage?.width
        val rendererPageHeight = rendererPage?.height
        val bitmap = Bitmap.createBitmap(
            rendererPageWidth!!,
            rendererPageHeight!!,
            Bitmap.Config.ARGB_8888
        )
        rendererPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        imageView?.setImageBitmap(bitmap)
        rendererPage.close()

    }


    fun copyAsset(
        assetManager: AssetManager,
        fromAssetPath: String?,
        toPath: String?
    ): Boolean {
        var `in`: InputStream? = null
        var out: OutputStream? = null
        return try {
            `in` = assetManager.open(fromAssetPath!!)
            File(toPath).createNewFile()
            out = FileOutputStream(toPath)
            copyFile(`in`, out)
            `in`.close()
            `in` = null
            out.flush()
            out.close()
            out = null
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    @Throws(IOException::class)
    fun copyFile(`in`: InputStream, out: OutputStream) {
        val buffer = ByteArray(1024)
        var read: Int?=0
        while (`in`.read(buffer).also({ read = it }) != -1) {
            out.write(buffer, 0, read!!)
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        scaleGestureDetector?.onTouchEvent(event)
        return super.onTouchEvent(event)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onStop() {
        try{
            pdfRenderer?.close()
            fileDescriptor?.close()
        }catch (e:Exception){
            e.printStackTrace()
        }
        super.onStop()
    }
}
