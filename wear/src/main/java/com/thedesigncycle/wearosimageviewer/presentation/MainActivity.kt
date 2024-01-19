
package com.thedesigncycle.wearosimageviewer.presentation

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.WindowManager
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.wearable.ChannelClient
import com.google.android.gms.wearable.ChannelClient.ChannelCallback
import com.google.android.gms.wearable.Wearable
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso
import com.thedesigncycle.wearosimageviewer.R
import java.io.File


class MainActivity : ComponentActivity() {


    private lateinit var imgView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContentView(R.layout.main)

        setTheme(android.R.style.Theme_DeviceDefault)
        imgView = findViewById(R.id.img_view)


        val channelClient = Wearable.getChannelClient(this)
        val ccb: ChannelCallback = object : ChannelCallback() {

            val file =
                File(applicationContext.getFileStreamPath("temp").path)
            val uri = Uri.fromFile(file)

            override fun onChannelOpened(c: ChannelClient.Channel) {

                super.onChannelOpened(c)

                channelClient.receiveFile(c, uri, false)
            }

            override fun onChannelClosed(channel: ChannelClient.Channel, i: Int, i1: Int) {
                super.onChannelClosed(channel, i, i1)

                Picasso.with(this@MainActivity).load(uri).memoryPolicy(MemoryPolicy.NO_CACHE).into(imgView)

            }

            override fun onInputClosed(p0: ChannelClient.Channel, p1: Int, p2: Int) {
                super.onInputClosed(p0, p1, p2)
                channelClient.close(p0)
            }


        }
        channelClient.registerChannelCallback(ccb)
    }


}

