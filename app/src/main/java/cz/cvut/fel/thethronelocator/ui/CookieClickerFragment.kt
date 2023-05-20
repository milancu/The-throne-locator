package cz.cvut.fel.thethronelocator.ui

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.material.button.MaterialButton
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import cz.cvut.fel.thethronelocator.R
import cz.cvut.fel.thethronelocator.auth.GoogleAuthClient
import cz.cvut.fel.thethronelocator.model.User
import cz.cvut.fel.thethronelocator.repository.UserRepository
import cz.cvut.fel.thethronelocator.databinding.FragmentCookieClickerBinding
import kotlin.random.Random


class CookieClickerFragment : Fragment(R.layout.fragment_cookie_clicker) {
    private lateinit var binding: FragmentCookieClickerBinding
    private var clickCount = 0
    private var isTimerRunning = false
    private var playAgain = true
    private lateinit var countDownTimer: CountDownTimer
    private lateinit var containerLayout: ConstraintLayout
    private lateinit var signInClient: SignInClient
    private lateinit var googleAuthClient: GoogleAuthClient
    private var screenWidth: Int = 0
    private var screenHeight: Int = 0
    private val userRepository = UserRepository()
    private lateinit var currentUser: User
    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val fragmentBinding = FragmentCookieClickerBinding.inflate(inflater, container, false)
        binding = fragmentBinding
        return fragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        signInClient = Identity.getSignInClient(requireActivity())
        googleAuthClient = GoogleAuthClient(requireActivity(), signInClient)
        val textCounter = binding.textCounter
        val playButton = binding.buttonPlayAgain
        val leaderboardButton = binding.buttonShowLeaderBoard
        val texRecord = binding.textRecord
        containerLayout = binding.cookieClickerLayout


        userRepository.getUserById(googleAuthClient.getUser()!!.userId, callback = {
            if (it != null) {
                currentUser = it
                texRecord.text = ("Your highest score: ${currentUser.record}")
            }
        })

        leaderboardButton.setOnClickListener {
            navController.navigate(R.id.action_cookieClicker_to_leaderboard)
            playAgain = true
        }

        textCounter.setOnClickListener {
            if (!isTimerRunning && playAgain) {
                startTimer()
            } else if (playAgain) {
                clickCount++
                updateCounterText()


                val imageView = createImageView()
                containerLayout.addView(imageView)


                screenWidth = Resources.getSystem().displayMetrics.widthPixels
                screenHeight = Resources.getSystem().displayMetrics.heightPixels

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
            val textView = binding.textViewCanDoBetter
            val textCounter = binding.textCounter
            val textTimer = binding.textTimer

            playAgain = true
            clickCount = 0

            textView.visibility = View.GONE
            playButton.visibility = View.GONE
            textCounter.text = 0.toString()
            textTimer.text = 15.toString()
        }
    }

    private fun createImageView(): ImageView {
        val imageView = ImageView(context)
        imageView.setImageResource(R.drawable.toilet_paper)
        imageView.layoutParams = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        imageView.x = (containerLayout.width / 2 - 140).toFloat()
        imageView.y = (containerLayout.height / 2 - 100).toFloat()
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
                val textTimer = binding.textTimer
                val seconds = ((millisUntilFinished / 1000) % 60)
                textTimer.text = (seconds.toString())
            }

            override fun onFinish() {
                val playButton = binding.buttonPlayAgain
                val texRecord = binding.textRecord
                val textView = binding.textViewCanDoBetter

                isTimerRunning = false
                playAgain = false

                if (clickCount > currentUser.record!!) {
                    userRepository.setNewRecord(currentUser.id!!, clickCount)
                }

                playButton.visibility = View.VISIBLE
                if (clickCount > currentUser.record!!) {
                    texRecord.text = ("Your highest score: $clickCount")
                    textView.visibility = View.GONE

                } else {
                    texRecord.text = ("Your highest score: ${currentUser.record}")
                    textView.visibility = View.VISIBLE
                }
            }
        }

        countDownTimer.start()
        isTimerRunning = true
    }

    private fun updateCounterText() {
        val textCounter = binding.textCounter
        textCounter.text = clickCount.toString()
    }
}