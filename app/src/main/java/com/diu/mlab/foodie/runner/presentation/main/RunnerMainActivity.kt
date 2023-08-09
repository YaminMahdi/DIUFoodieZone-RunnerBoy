package com.diu.mlab.foodie.runner.presentation.main

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import com.diu.mlab.foodie.runner.R
import com.diu.mlab.foodie.runner.databinding.ActivityRunnerMainBinding
import com.diu.mlab.foodie.runner.util.changeStatusBarColor
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RunnerMainActivity : AppCompatActivity() {
    private val viewModel : MainViewModel by viewModels()
    private lateinit var binding: ActivityRunnerMainBinding

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d("TAG", "Notification PERMISSION_GRANTED")
        } else {
            Log.d("TAG", "Notification PERMISSION_DENIED")
            Toast.makeText(this, "Must give notification permission", Toast.LENGTH_SHORT).show()
            askNotificationPermission()
        }
    }

    private fun askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        requestPermissionLauncher.launch(Manifest.permission.CALL_PHONE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRunnerMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        askNotificationPermission()
        Firebase.messaging.subscribeToTopic("notifyRunner")

        val manager: FragmentManager = supportFragmentManager

        binding.bubbleTabBar.addBubbleListener { id ->
            when(id){
                R.id.current -> {
                    binding.topView.setBackgroundColor(this.getColor(R.color.tia))
                    this.changeStatusBarColor(R.color.tiaX,false)
                    manager.beginTransaction()
                        .replace(binding.mainFragment.id, DeliveryFragment.newInstance("current"))
                        .commit()
                }
                R.id.history -> {
                    binding.topView.setBackgroundColor(this.getColor(R.color.blueX))
                    this.changeStatusBarColor(R.color.blueZ,false)
                    manager.beginTransaction()
                        .replace(binding.mainFragment.id, DeliveryFragment.newInstance("completed"))
                        .commit()
                }
                R.id.profile -> {
                    binding.topView.setBackgroundColor(this.getColor(R.color.tia))
                    this.changeStatusBarColor(R.color.tiaX,false)
                    manager.beginTransaction()
                        .replace(binding.mainFragment.id, ProfileFragment())
                        .commit()
                }
            }
        }

    }

    override fun onResume() {
        super.onResume()
        viewModel.getMyProfile {
            MainScope().launch {
                Toast.makeText(this@RunnerMainActivity, it, Toast.LENGTH_SHORT).show()
            }
        }
    }
}