package com.lessons.convertorjpgtopng

import android.content.ContentResolver
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import moxy.MvpPresenter
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.Executors
import java.util.concurrent.Future


class MainPresenter : MvpPresenter<MainView>() {
    private val EXT = ".png"
    private val disposables = CompositeDisposable()
    private var future: Future<*>? = null

    fun convert(contentResolver: ContentResolver, storageDir: File, uri: Uri) {
        Completable.fromCallable { convertFile(contentResolver, storageDir, uri) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { viewState.setResult(true) },
                { viewState.setError() }
            )
            .addTo(disposables)
    }

    private fun convertFile(contentResolver: ContentResolver, storageDir: File, uri: Uri) {
        future = Executors.newSingleThreadExecutor().submit {
            Thread.sleep(5000)
            val bitmapImage = MediaStore.Images.Media.getBitmap(contentResolver, uri)
            storageDir.mkdirs()
            try {
                val fos = FileOutputStream(File(storageDir, uri.lastPathSegment + EXT))
                bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos)
                fos.close()
            } catch (e: Exception) {
                e.printStackTrace()
                throw IllegalArgumentException("error into convertation")
            }
        }
        future?.get()
    }

    fun cancelConvert() {
        if (disposables.isDisposed != true) {
            future?.takeIf { task -> !task.isDone }
                ?.takeIf { task -> !task.isCancelled }
                ?.cancel(true)
            disposables.clear()
            viewState.setResult(false)
        }
    }

    override fun onDestroy() {
        cancelConvert()
    }
}

