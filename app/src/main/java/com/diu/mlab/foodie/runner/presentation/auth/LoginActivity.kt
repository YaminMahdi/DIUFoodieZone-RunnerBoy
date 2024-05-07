package com.diu.mlab.foodie.runner.presentation.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.diu.mlab.foodie.runner.databinding.ActivityLoginBinding
import com.diu.mlab.foodie.runner.domain.RequestState
import com.diu.mlab.foodie.runner.presentation.main.RunnerMainActivity
import com.diu.mlab.foodie.runner.util.setBounceClickListener
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {
    private val viewModel : AuthViewModel by viewModels()
    private lateinit var binding: ActivityLoginBinding

//    private var resultLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
//        if (result.resultCode == Activity.RESULT_OK) {
//            val data: Intent? = result.data
//            val credential: SignInCredential = Identity.getSignInClient(this).getSignInCredentialFromIntent(data)
//            viewModel.firebaseLogin(credential,{
//                startActivity(Intent(this,RunnerMainActivity::class.java))
//            }){
//                Log.e("TAG", "failed: $it")
//                MainScope().launch {
//                    Toast.makeText(this@LoginActivity, it, Toast.LENGTH_SHORT).show()
//                }
//            }
//        }
//        else if (result.resultCode == Activity.RESULT_CANCELED){
//            Log.d("TAG", "RESULT_CANCELED")
//        }
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.signInBtn.setBounceClickListener {
            viewModel.googleSignIn(this){result ->
                when(result){
                    is RequestState.Error -> {
                        Log.e("TAG", "failed: ${result.error}")
                        if(result.code!= 20)
                            Toast.makeText(this, result.error, Toast.LENGTH_SHORT).show()
                    }
                    is RequestState.Success -> {
                        viewModel.firebaseLogin(result.data,
                            success = {
                                startActivity(Intent(this,RunnerMainActivity::class.java))
                            },
                            failed = {
                                Log.e("TAG", "failed: $it")
                                MainScope().launch {
                                    Toast.makeText(this@LoginActivity, it, Toast.LENGTH_SHORT).show()
                                }
                            })
                    }
                }
            }
        }
        binding.signUpBtn.setBounceClickListener {
            startActivity(Intent(this,RegistrationActivity::class.java))
        }
        viewModel.loadingVisibility.observe(this){
            binding.loadingLayout.visibility = if(it) View.VISIBLE else View.GONE
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