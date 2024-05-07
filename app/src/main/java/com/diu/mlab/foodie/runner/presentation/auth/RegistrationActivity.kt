package com.diu.mlab.foodie.runner.presentation.auth

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.diu.mlab.foodie.runner.databinding.ActivityRegistrationBinding
import com.diu.mlab.foodie.runner.domain.RequestState
import com.diu.mlab.foodie.runner.domain.model.FoodieUser
import com.diu.mlab.foodie.runner.presentation.main.RunnerMainActivity
import com.diu.mlab.foodie.runner.util.setBounceClickListener
import com.diu.mlab.foodie.runner.util.transformedEmailId
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RegistrationActivity : AppCompatActivity() {
    private val viewModel : AuthViewModel by viewModels()
    private lateinit var binding: ActivityRegistrationBinding

    private lateinit var runner: FoodieUser
    private var logoUri : Uri?= null


//    private var resultLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
//        if (result.resultCode == Activity.RESULT_OK) {
//            val data: Intent? = result.data
//            val credential: SignInCredential = Identity.getSignInClient(this).getSignInCredentialFromIntent(data)
//            viewModel.firebaseSignup(credential,runner.copy(email = credential.id.transformedEmailId()),{
//                startActivity(Intent(this, RunnerMainActivity::class.java))
//                Log.d("TAG", "success:")
//            }){
//                Log.e("TAG", "failed: $it")
//                MainScope().launch {
//                    Toast.makeText(this@RegistrationActivity, it, Toast.LENGTH_SHORT).show()
//                }
//            }
//        }
//        else if (result.resultCode == Activity.RESULT_CANCELED){
//            Log.d("TAG", "RESULT_CANCELED")
//        }
//    }

    private var galleryLauncher4pic = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            logoUri = uri
            binding.pic.setImageURI(logoUri)
            Log.d("TAG", "pre ${logoUri?.path} $logoUri")

//            var logo = copyUriToFile(Uri.parse(uri.toString()))
//            GlobalScope.launch(Dispatchers.IO){
//                logo = Compressor.compress(this@RegistrationActivity, logo) {
//                    default(height = 360, width = 360, format = Bitmap.CompressFormat.JPEG)
//                }
//                val logoLink = Firebase.storage.reference.child("test").child("test.jpg")
//                    .putFile(Uri.fromFile(logo)).await().storage.downloadUrl.await()
//                Log.d("TAG", "post ${logoLink.path} $logoLink")
//
//            }
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
            viewModel.googleSignIn(this, runner){result->
                when(result){
                    is RequestState.Error -> {
                        Log.e("TAG", "failed: ${result.error}")
                        if(result.code!= 20)
                            Toast.makeText(this, result.error, Toast.LENGTH_SHORT).show()
                    }
                    is RequestState.Success -> {
                        viewModel.firebaseSignup(result.data, runner.copy(email = result.data.id.transformedEmailId()),
                            success = {
                                startActivity(Intent(this, RunnerMainActivity::class.java))
                                finish()
                                Log.d("TAG", "success:")
                            },
                            failed = {
                                Log.e("TAG", "failed: $it")
                                MainScope().launch {
                                    Toast.makeText(this@RegistrationActivity, it, Toast.LENGTH_SHORT).show()
                                }
                            })
                    }
                }
            }
            viewModel.loadingVisibility.observe(this){
                binding.loadingLayout.visibility = if(it) View.VISIBLE else View.GONE
            }

        }


    }
}