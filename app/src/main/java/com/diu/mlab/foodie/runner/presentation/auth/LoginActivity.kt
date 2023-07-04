package com.diu.mlab.foodie.runner.presentation.auth

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.diu.mlab.foodie.runner.R
import com.diu.mlab.foodie.runner.databinding.ActivityLoginBinding
import com.diu.mlab.foodie.runner.presentation.main.RunnerMainActivity
import com.diu.mlab.foodie.runner.util.setBounceClickListener
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInCredential
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {
    private val viewModel : AuthViewModel by viewModels()
    private lateinit var binding: ActivityLoginBinding

    private var resultLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val credential: SignInCredential = Identity.getSignInClient(this).getSignInCredentialFromIntent(data)
            viewModel.firebaseLogin(credential,{
                startActivity(Intent(this,RunnerMainActivity::class.java))
            }){
                Log.e("TAG", "failed: $it")
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }
        else if (result.resultCode == Activity.RESULT_CANCELED){
            Log.d("TAG", "RESULT_CANCELED")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.signInBtn.setBounceClickListener {
            viewModel.googleSignIn(this,resultLauncher){msg ->
                Log.d("TAG", "onCreate: $msg")
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            }
        }
        binding.signUpBtn.setBounceClickListener {
            startActivity(Intent(this,RegistrationActivity::class.java))
        }
    }

    override fun onStart() {
        super.onStart()
        if(Firebase.auth.currentUser != null) {
            startActivity(Intent(this, RunnerMainActivity::class.java))
            this.finish()
        }
    }
}