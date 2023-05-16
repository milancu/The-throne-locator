package cz.cvut.fel.thethronelocator.ui

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.os.CountDownTimer
import android.util.DisplayMetrics
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentActivity
import com.google.android.material.button.MaterialButton
import cz.cvut.fel.thethronelocator.R
import kotlin.random.Random

class CookieClicker : FragmentActivity() {
    private var clickCount = 0
    private var isTimerRunning = false
    private var playAgain = true
    private lateinit var countDownTimer: CountDownTimer
    private lateinit var containerLayout: ConstraintLayout
    private var screenWidth: Int = 0
    private var screenHeight: Int = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.cookie_clicker)

        val textCounter = findViewById<TextView>(R.id.text_counter)
        val playButton = findViewById<MaterialButton>(R.id.button_play_again)
        containerLayout = findViewById(R.id.cookie_clicker_layout)


        textCounter.setOnClickListener {
            if (!isTimerRunning && playAgain) {
                startTimer()
            } else if (playAgain) {
                clickCount++
                updateCounterText()


                val imageView = createImageView()
                containerLayout.addView(imageView)


                val displayMetrics = DisplayMetrics()
                windowManager.defaultDisplay.getMetrics(displayMetrics)
                screenWidth = displayMetrics.widthPixels
                screenHeight = displayMetrics.heightPixels

                val textCounterX = textCounter.x + 50 //fix offset
                val textCounterY = textCounter.y + 900

                imageView.x = textCounterX
                imageView.y = textCounterY
                imageView.rotation = Random.nextInt(360).toFloat()

                val animX = ObjectAnimator.ofFloat(
                    imageView,
                    "translationX",
                    getRandomTranslation(-screenWidth.toFloat(), screenWidth.toFloat())
                )
                val animY = ObjectAnimator.ofFloat(
                    imageView,
                    "translationY",
                    getRandomTranslation(-screenHeight.toFloat(), screenHeight.toFloat())
                )
                val alphaAnim = ObjectAnimator.ofFloat(
                    imageView,
                    "alpha",
                    0.2f,
                    0.8f,
                    1f
                )
                val scaleXAnim = ObjectAnimator.ofFloat(
                    imageView,
                    "scaleX",
                    0.4f,
                    1.2f
                )
                val scaleYAnim = ObjectAnimator.ofFloat(
                    imageView,
                    "scaleY",
                    0.4f,
                    1.2f
                )
                val rotateAnim = ObjectAnimator.ofFloat(
                    imageView,
                    "rotation",
                    Random.nextInt(360).toFloat()
                )

                val set = AnimatorSet()
                set.playTogether(animX, animY, alphaAnim, scaleXAnim, scaleYAnim, rotateAnim)
                set.duration = 1500
                set.start()

                set.addListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(p0: Animator) {
                        TODO("Not yet implemented")
                    }

                    override fun onAnimationEnd(p0: Animator) {
                        containerLayout.removeView(imageView)
                    }

                    override fun onAnimationCancel(p0: Animator) {
                        TODO("Not yet implemented")
                    }

                    override fun onAnimationRepeat(p0: Animator) {
                        TODO("Not yet implemented")
                    }
                })
            }
        }

        playButton.setOnClickListener {
            val textCounter = findViewById<TextView>(R.id.text_counter)
            val textTimer = findViewById<TextView>(R.id.text_timer)

            playAgain = true
            clickCount = 0

            playButton.visibility = View.GONE
            textCounter.text = 0.toString()
            textTimer.text = 15.toString()
        }
    }

    private fun createImageView(): ImageView {
        val imageView = ImageView(this)
        imageView.setImageResource(R.drawable.toilet_paper)
        imageView.layoutParams = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        return imageView
    }

    private fun getRandomTranslation(min: Float, max: Float): Float {
        return Random.nextFloat() * (max - min) + min
    }

    private fun startTimer() {
        val millisInFuture = 14000L
        val countDownInterval = 1000L

        countDownTimer = object : CountDownTimer(millisInFuture, countDownInterval) {
            override fun onTick(millisUntilFinished: Long) {
                val textTimer = findViewById<TextView>(R.id.text_timer)
                val seconds = ((millisUntilFinished / 1000) % 60)
                textTimer.text = (seconds.toString())
            }

            override fun onFinish() {
                val playButton = findViewById<MaterialButton>(R.id.button_play_again)
                val texRecord = findViewById<TextView>(R.id.text_record)

                isTimerRunning = false
                playAgain = false

                playButton.visibility = View.VISIBLE
                texRecord.text = ("Your score: ${clickCount.toString()}")
            }
        }

        countDownTimer.start()
        isTimerRunning = true
    }

    private fun updateCounterText() {
        val textCounter = findViewById<TextView>(R.id.text_counter)
        textCounter.text = clickCount.toString()
    }
}