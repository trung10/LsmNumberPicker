package com.trungpd.LmNumberPicker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.trungpd.LmNumberPicker.R

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val numpicker = findViewById<LsmNumberPicker>(R.id.numpicker)
        numpicker.wrapSelectorWheel = false

    }
}