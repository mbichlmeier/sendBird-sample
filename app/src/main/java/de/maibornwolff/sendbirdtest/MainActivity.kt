package de.maibornwolff.sendbirdtest

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import timber.log.Timber

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configureLogger()
        setContentView(R.layout.activity_main)
        val chatFragmentContainer = MainFragment()
        supportFragmentManager.beginTransaction().replace(R.id.fragment_layout, chatFragmentContainer).commit()
    }


    private fun configureLogger() {
        Timber.plant(Timber.DebugTree())
    }
}