package com.testdemo.dsaapp

import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.testdemo.dsaapp.databinding.ActivityDownloadFileBinding
import com.tonyodev.fetch2.Download
import com.tonyodev.fetch2.Error
import com.tonyodev.fetch2.Fetch
import com.tonyodev.fetch2.FetchConfiguration
import com.tonyodev.fetch2.FetchListener
import com.tonyodev.fetch2.Request
import com.tonyodev.fetch2core.DownloadBlock
import java.io.File


class DownloadFileActivity : AppCompatActivity() {
    private val TAG = "DownloadFileActivity"
    private val binding by lazy { ActivityDownloadFileBinding.inflate(layoutInflater) }
    private var fetch: Fetch? = null

    val fetchListener = object : FetchListener {
        override fun onQueued(download: Download, waitingOnNetwork: Boolean) {
            Log.e("Fetch", "Download queued: ${download.file}")

        }

        override fun onCompleted(download: Download) {
            Log.e("Fetch", "Download completed: ${download.file}")

        }


        override fun onProgress(
            download: Download,
            etaInMilliSeconds: Long,
            downloadedBytesPerSecond: Long
        ) {
            Log.e("Fetch", "Download progress: ${download.progress}%")

        }

        override fun onStarted(
            download: Download,
            downloadBlocks: List<DownloadBlock>,
            totalBlocks: Int
        ) {
            Log.e(TAG, "onStarted: ${download.file}")
        }

        override fun onWaitingNetwork(download: Download) {
            Log.e(TAG, "onWaitingNetwork: ${download.progress}")
        }
        override fun onAdded(download: Download) {}
        override fun onCancelled(download: Download) {}
        override fun onRemoved(download: Download) {}
        override fun onDeleted(download: Download) {}
        override fun onPaused(download: Download) {}
        override fun onResumed(download: Download) {}

        override fun onDownloadBlockUpdated(
            download: Download,
            downloadBlock: DownloadBlock,
            totalBlocks: Int
        ) {
            Log.e(TAG, "onDownloadBlockUpdated: ${download.progress}")
            Log.e(TAG, "onDownloadBlockUpdated: ${downloadBlock.downloadId}")
        }

        override fun onError(download: Download, error: Error, throwable: Throwable?) {
            Log.e(TAG, "onError: $error")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val fetchConfiguration: FetchConfiguration =
            FetchConfiguration.Builder(this).setDownloadConcurrentLimit(3).build()
        fetch = Fetch.Impl.getInstance(fetchConfiguration)
        val videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/VolkswagenGTIReview.mp4"
        binding.btnStart.setOnClickListener {
         //   val filePath = "${getExternalFilesDir(null)}/downloads/VolkswagenGTIReview.mp4"
            val filePath = "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)}/VolkswagenGTIReview.mp4"
            val file =File(filePath)
            if (file.exists()){
                Log.e(TAG, "Start: Already Exist \n$filePath")
            }else {
                startDownloading(videoUrl, filePath)
            }
        }
        binding.btnStop.setOnClickListener {
            if (fetch != null) {
                fetch!!.removeListener(fetchListener)
            }
        }
    }

    private fun startDownloading(videoUrl: String, filePath: String) {
        val file = File(filePath)
        file.parentFile?.mkdirs()
        val request = Request(videoUrl, filePath)
        fetch!!.enqueue(request, { updatedRequest ->
            Log.e(TAG, "startDownloading: ${updatedRequest.id}")
        },
            { error ->
                Log.e(TAG, "startDownloading Error: ${error.name}")
            })
        fetch!!.addListener(fetchListener)
    }


    override fun onDestroy() {
        if (fetch != null) {
            fetch!!.close()
        }
        super.onDestroy()
    }
}