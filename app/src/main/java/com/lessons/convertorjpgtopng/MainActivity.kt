package com.lessons.convertorjpgtopng

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import by.kirich1409.viewbindingdelegate.viewBinding
import com.lessons.convertorjpgtopng.databinding.ActivityMainBinding
import moxy.MvpAppCompatActivity
import moxy.ktx.moxyPresenter
import java.io.File
import java.util.*


class MainActivity : MvpAppCompatActivity(), MainView {
    private val PICK_IMAGES = 112
    private val PERMISSIONS_REQUEST = 110
    private val DIR_NAME = "converted"
    private val vb: ActivityMainBinding by viewBinding()
    private val presenter by moxyPresenter { MainPresenter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setContentView(vb.root)
        vb.selectImage.setOnClickListener {
            if (isHasPermission())
                pickImagesIntent()
        }
        vb.cancel.setOnClickListener { presenter.cancelConvert() }
        isHasPermission()
    }

    fun pickImagesIntent() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        startActivityForResult(
            Intent.createChooser(
                intent,
                resources.getString(R.string.select_picture)
            ), PICK_IMAGES
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_IMAGES) {
                val returnUri: Uri? = data?.getData()
                val storageDir = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    DIR_NAME
                )
                vb.cancel.visibility = View.VISIBLE
                vb.selectImage.visibility = View.GONE
                presenter.convert(contentResolver, storageDir, returnUri!!)
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun isHasPermission(): Boolean {
        val permission = ActivityCompat.checkSelfPermission(
            this@MainActivity,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        val PERMISSIONS_STORAGE = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this@MainActivity,
                PERMISSIONS_STORAGE,
                PERMISSIONS_REQUEST
            )
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSIONS_REQUEST -> {
                if (grantResults.size <= 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(
                        this,
                        resources.getString(R.string.need_permission),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun setResult(completed: Boolean) {
        Toast.makeText(
            this,
            resources.getString(if (completed) R.string.ready else R.string.canceled),
            Toast.LENGTH_LONG
        ).show()
        vb.cancel.visibility = View.GONE
        vb.selectImage.visibility = View.VISIBLE
    }

    override fun setError() {
        Toast.makeText(
            this,
            resources.getString(R.string.error),
            Toast.LENGTH_LONG
        ).show()
        vb.cancel.visibility = View.GONE
        vb.selectImage.visibility = View.VISIBLE
    }
}