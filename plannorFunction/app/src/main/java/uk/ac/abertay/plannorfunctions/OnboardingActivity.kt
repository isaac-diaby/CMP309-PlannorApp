package uk.ac.abertay.plannorfunctions


import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.ExperimentalAnimationApi
import uk.ac.abertay.plannorfunctions.LoginActivity
import uk.ac.abertay.plannorfunctions.databinding.OnboardingMainBinding
import com.google.firebase.auth.FirebaseAuth
import java.util.*


class OnboardingActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: OnboardingMainBinding
    private var currentIndex = 0
    private var loopDescCtr: Timer = Timer()

    @OptIn(ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = OnboardingMainBinding.inflate(layoutInflater)

        setContentView(binding.root)
        // check if user is logged in
        auth =  FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Log.d("OnboardingActivity", "onCreate: User is not logged in")
        } else {
            Log.d("OnboardingActivity", "onCreate: User is logged in, sending to main activity")
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
//        Navigate to the next screen (login)
        val nextBtn = binding.nextBtn
        nextBtn.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        //        Animate the onboarding messages
        val txtSwitcher = binding.subtextctx
        txtSwitcher.setInAnimation(applicationContext, android.R.anim.slide_in_left)
        txtSwitcher.setOutAnimation(applicationContext, android.R.anim.slide_out_right)
        val textToShow = arrayOf(
            getString(R.string.splash_desc_one),
            getString(R.string.splash_desc_two),
            getString(R.string.splash_desc_three)
        )
        txtSwitcher.setText(textToShow[currentIndex])
        // loop through the text until the user clicks next with a 3 second delay between each message
        loopDescCtr.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                Log.d("OnboardingActivity", "onCreate: $currentIndex")
                currentIndex++
                // If index reaches maximum reset it
                if (currentIndex == (textToShow.size)) {
                    currentIndex = 0
                }
                runOnUiThread { txtSwitcher.setText(textToShow[currentIndex]) }
            }
        }, 3000, 3000)
    }
    override fun onStop() {
        super.onStop()
        loopDescCtr.purge()
    }
    override fun onDestroy() {
        super.onDestroy()
        loopDescCtr.cancel()
        finish()
    }
}