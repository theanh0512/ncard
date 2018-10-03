package com.user.ncard.ui.catalogue.utils

import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler
import com.github.hiteshsondhi88.libffmpeg.FFmpeg
import com.github.hiteshsondhi88.libffmpeg.FFmpegExecuteResponseHandler
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException
import com.user.ncard.R
import me.shaohui.advancedluban.Luban
import me.shaohui.advancedluban.OnMultiCompressListener
import java.io.File

/**
 * Created by trong-android-dev on 8/12/17.
 */
class CompressionUtil(val context: Context, val ffmpeg: FFmpeg) {

    val TAG = "CompressionUtil"

    init {
        loadFFMpegBinary()
    }

    private fun loadFFMpegBinary() {
        try {
            ffmpeg?.loadBinary(object : LoadBinaryResponseHandler() {
                override fun onFailure() {
                    showUnsupportedExceptionDialog()
                }
            })
        } catch (e: FFmpegNotSupportedException) {
            showUnsupportedExceptionDialog()
        }
    }

    private fun showUnsupportedExceptionDialog() {
        AlertDialog.Builder(context)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(context.getString(R.string.device_not_supported))
                .setMessage(context.getString(R.string.device_not_supported_message))
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok) { dialog, which -> }
                .create()
                .show()
    }

    /**
     * Compress images using Luban lib
     */
    fun compressImage(imagesList: List<String>, listener: OnMultiCompressListener) {

        val filesList = imagesList.map { File(it) }
        Luban.compress(context, filesList)
                .setMaxSize(1000)                // limit the final image size（unit：Kb）
                .setMaxHeight(1920 * 2)             // limit image height
                .setMaxWidth(1080 * 2)              // limit image width
                .putGear(Luban.CUSTOM_GEAR)     // use CUSTOM GEAR compression mode
                .launch(object : OnMultiCompressListener {
                    override fun onSuccess(fileList: MutableList<File>?) {
                        listener.onSuccess(fileList)
                    }

                    override fun onError(e: Throwable?) {
                        listener.onError(e)
                    }

                    override fun onStart() {
                        listener.onStart()
                    }
                })
    }

    fun setupVideoCompress() {
        this.videoOutputPath = ""
        this.imageExtractedFromVideoPath = ""
    }

    lateinit var videoOutputPath: String
    lateinit var imageExtractedFromVideoPath: String
    /**
     * Compress video using FFmpeg
     */
    fun compressVideo(videoInputPath: String, listener: FFmpegExecuteResponseHandler) {
        val videoFileInput = File(videoInputPath)
        videoOutputPath = Functions.getDirectory().absolutePath + "/" + videoFileInput.name
        val cmdFormat = "-y -i %s -r 20 -c:v libx264 -preset ultrafast -c:a copy -me_method zero -tune fastdecode -tune zerolatency -strict -2 -b:v 3000k -pix_fmt yuv420p %s"
        val command = String.format(cmdFormat, videoFileInput.absolutePath, videoOutputPath)

        val cmd = command.split(" ".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
        if (cmd.size != 0) {
            execFFmpegBinary(cmd, listener)
        } else {
            Toast.makeText(context, context.getString(R.string.empty_command_toast), Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Extract first image from video using FFmpeg
     */
    fun extractImageFromVideo(videoInputPath: String, listener: FFmpegExecuteResponseHandler) {
        val videoFileInput = File(videoInputPath)
        imageExtractedFromVideoPath = Functions.getDirectory().absolutePath + "/" + videoFileInput.name.substring(0, videoFileInput.name.lastIndexOf(".")) + ".jpg"
        val cmdFormat = "-y -i %s -ss 00:00:01.000 -vframes 1 %s"
        val command = String.format(cmdFormat, videoFileInput.absolutePath, imageExtractedFromVideoPath)

        val cmd = command.split(" ".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
        if (cmd.size != 0) {
            execFFmpegBinary(cmd, listener)
        } else {
            Toast.makeText(context, context.getString(R.string.empty_command_toast), Toast.LENGTH_LONG).show()
        }
    }


    /**
     * Execute FFmpeg command
     */
    private fun execFFmpegBinary(command: Array<String>, listener: FFmpegExecuteResponseHandler) {
        try {
            ffmpeg?.execute(command, object : ExecuteBinaryResponseHandler() {
                override fun onFailure(s: String?) {
                    Log.d(TAG, "FAILED with output : " + s!!)
                    listener?.onFailure(s)
                }

                override fun onSuccess(s: String?) {
                    Log.d(TAG, "SUCCESS with output : " + s!!)
                    listener.onSuccess(s)
                }

                override fun onProgress(s: String?) {
                    Log.d(TAG, "Started command : ffmpeg " + command)
                    Log.d(TAG, "progress : " + s!!)
                    listener.onProgress(s)
                }

                override fun onStart() {
                    Log.d(TAG, "Started command : ffmpeg " + command)
                    listener.onStart()
                }

                override fun onFinish() {
                    Log.d(TAG, "Finished command : ffmpeg " + command)
                    //Functions.saveMediaStore(activity.contentResolver, videoOutputPath, TYPE_VIDEO)
                    //Functions.saveMediaStore(activity.contentResolver, imageExtractedFromVideoPath, TYPE_IMAGES)
                    listener.onFinish()
                }
            })
        } catch (e: FFmpegCommandAlreadyRunningException) {
            // do nothing for now
            Log.d(TAG, e.message)
        }

    }

}