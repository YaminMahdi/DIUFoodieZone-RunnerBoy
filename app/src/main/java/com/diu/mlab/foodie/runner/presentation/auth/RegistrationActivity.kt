package com.diu.mlab.foodie.runner.presentation.auth

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.diu.mlab.foodie.runner.databinding.ActivityRegistrationBinding
import com.diu.mlab.foodie.runner.domain.model.FoodieUser
import com.diu.mlab.foodie.runner.presentation.main.RunnerMainActivity
import com.diu.mlab.foodie.runner.util.setBounceClickListener
import com.diu.mlab.foodie.runner.util.transformedEmailId
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInCredential
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegistrationActivity : AppCompatActivity() {
    private val viewModel : AuthViewModel by viewModels()
    private lateinit var binding: ActivityRegistrationBinding

    private lateinit var runner: FoodieUser
    private var logoUri : Uri?= null


    private var resultLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val credential: SignInCredential = Identity.getSignInClient(this).getSignInCredentialFromIntent(data)
            viewModel.firebaseSignup(credential,runner.copy(email = credential.id.transformedEmailId()),{
                startActivity(Intent(this, RunnerMainActivity::class.java))
                Log.d("TAG", "success:")
            }){
                Log.e("TAG", "failed: $it")
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }
        else if (result.resultCode == Activity.RESULT_CANCELED){
            Log.d("TAG", "RESULT_CANCELED")
        }
    }

    private var galleryLauncher4pic = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            logoUri = uri
            binding.pic.setImageURI(logoUri)
            Log.d("TAG", "${logoUri?.path}")
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnBack.setBounceClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        val photoPicker = PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        binding.pic.setOnClickListener { galleryLauncher4pic.launch(photoPicker) }
        binding.btnRegister.setBounceClickListener{
            runner = FoodieUser(
                nm = binding.shopNm.text.toString(),
                email = "",
                phone = binding.pnNo.text.toString(),
                userType = binding.type.text.toString(),
                pic = logoUri?.toString() ?: "",
            )
            viewModel.googleSignIn(this,resultLauncher, runner){
                Log.e("TAG", "failed: $it")
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }

        }


    }
}