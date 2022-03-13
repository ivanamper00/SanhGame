package com.bigteeti.sanhgame.ui.activity

import android.os.Bundle
import android.os.CountDownTimer
import com.bigteeti.sanhgame.R
import com.dakulangsakalam.customwebview.jump_code.presentation.JumpActivity
import com.dakulangsakalam.customwebview.jump_code.presentation.utils.splashAction

class SplashActivity : JumpActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val timer = object: CountDownTimer(3000, 1000){
            override fun onTick(p0: Long) { }
            override fun onFinish() {
                splashAction(true){ _, _ ->
                    startActivity(MainActivity.createIntent(this@SplashActivity))
                    finish()
                }
            }
        }
        timer.start()
    }
}