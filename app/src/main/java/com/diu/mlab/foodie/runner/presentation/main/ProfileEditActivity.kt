package com.diu.mlab.foodie.runner.presentation.main

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
import com.diu.mlab.foodie.runner.databinding.ActivityOrderInfoBinding
import com.diu.mlab.foodie.runner.databinding.ActivityProfileEditBinding
import com.diu.mlab.foodie.runner.domain.model.FoodieUser
import com.diu.mlab.foodie.runner.util.getDrawable
import com.diu.mlab.foodie.runner.util.setBounceClickListener
import com.diu.mlab.foodie.runner.util.transformedEmailId
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInCredential
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch


@AndroidEntryPoint
class ProfileEditActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileEditBinding
    private val viewModel : MainViewModel by viewModels()
    private var runnerInfo = FoodieUser()
    private var picUpdated: Boolean = false
    private var picUri : Uri?= null

    private var galleryLauncher4pic = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            picUri = uri
            binding.pic.setImageURI(picUri)
            Log.d("TAG", "pre ${picUri?.path} $picUri")
            picUpdated = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val photoPicker = PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        binding.pic.setOnClickListener { galleryLauncher4pic.launch(photoPicker) }


        viewModel.getMyProfile {
            MainScope().launch {
                Toast.makeText(this@ProfileEditActivity, it, Toast.LENGTH_SHORT).show()
            }
        }
        viewModel.myProfile.observe(this){ runner->
            runnerInfo = runner
            binding.nm.setText(runner.nm)
            binding.pnNo.setText(runner.phone)
            binding.type.setText(runner.userType,false)

            runner.pic.getDrawable { binding.pic.setImageDrawable(it) }
        }

        binding.btnSave.setBounceClickListener {
            onBackPressedDispatcher.onBackPressed()
            viewModel.updateMyProfile(
                runnerInfo.copy(
                    phone = binding.pnNo.text.toString(),
                    userType = binding.type.text.toString(),
                    pic = if(picUpdated) picUri.toString() else runnerInfo.pic
                ),picUpdated, {
                    MainScope().launch {
                        Toast.makeText(this@ProfileEditActivity, "Profile Updated", Toast.LENGTH_SHORT).show()
                    }
                },{
                    MainScope().launch {
                        Toast.makeText(this@ProfileEditActivity, it, Toast.LENGTH_SHORT).show()
                    }
                })
        }

        binding.btnBack.setBounceClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }
}