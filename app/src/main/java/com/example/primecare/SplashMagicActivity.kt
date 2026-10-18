package com.example.primecare

import android.content.Intent
import android.os.Bundle
import android.view.animation.AnticipateInterpolator
import android.view.animation.OvershootInterpolator
import androidx.appcompat.app.AppCompatActivity
import android.widget.ImageView
import android.widget.LinearLayout

class SplashMagicActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_magic)

        val blob = findViewById<ImageView>(R.id.blobView)
        val logo = findViewById<LinearLayout>(R.id.logoOnBlob)

        // Animate like Ventures video: blob grows fast to cover screen
        blob.animate()
            .scaleX(6.5f).scaleY(6.5f)
            .setDuration(800)
            .setInterpolator(OvershootInterpolator())
            .withEndAction {
                // Show PrimeCare logo on blue blob
                logo.animate().alpha(1f).setDuration(300).start()

                // After 1.5 sec, go to your REAL login page
                logo.postDelayed({
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                }, 1500)
            }.start()
    }
}