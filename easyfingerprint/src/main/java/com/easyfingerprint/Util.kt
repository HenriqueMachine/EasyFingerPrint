package com.easyfingerprint

import android.os.Build

object Util {

    fun isAndroidGraterM():Boolean{
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
    }

    fun isAndroidGraterLOLLIPOP():Boolean{
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
    }
}