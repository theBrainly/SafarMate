package com.example.mypp

import android.app.Application
import com.example.mypp.api.RetrofitClient

class SafarMateApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize RetrofitClient
        RetrofitClient.init(this)
    }
}
