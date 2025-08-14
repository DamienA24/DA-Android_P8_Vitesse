package com.quizocr.vitesse

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.ui.AppBarConfiguration
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.quizocr.vitesse.databinding.ActivityMainBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var keepSplashScreenOn = true

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        splashScreen.setKeepOnScreenCondition { keepSplashScreenOn }

        initializeApp()

        setSupportActionBar(binding.toolbar)

        setupFab()

        setupSplashScreenExitAnimation(splashScreen)
    }

    private fun initializeApp() {
        lifecycleScope.launch {
            try {
                delay(2000L)
                keepSplashScreenOn = false

            } catch (e: Exception) {
                handleInitializationError(e)
            }
        }
    }

    private fun setupFab() {
        binding.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null)
                .setAnchorView(R.id.fab)
                .show()
        }
    }

    private fun setupSplashScreenExitAnimation(splashScreen: androidx.core.splashscreen.SplashScreen) {
        splashScreen.setOnExitAnimationListener { splashScreenView ->
            splashScreenView.view.animate()
                .alpha(0f)
                .scaleX(0.8f)
                .scaleY(0.8f)
                .setDuration(500L)
                .withEndAction {
                    splashScreenView.remove()
                }
                .start()
        }
    }

    private fun handleInitializationError(exception: Exception? = null) {
        exception?.printStackTrace()
        lifecycleScope.launch {
            keepSplashScreenOn = false

            delay(100)

            Snackbar.make(
                binding.root,
                R.string.error_loading_app,
                Snackbar.LENGTH_LONG
            ).setAction(R.string.retry) {
                recreate()
            }.show()
        }
    }
}