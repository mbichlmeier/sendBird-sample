package de.maibornwolff.sendbirdtest

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import timber.log.Timber

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configureLogger()
        setContentView(R.layout.activity_main)
    }


    private fun configureLogger() {
        Timber.plant(Timber.DebugTree())
    }
}