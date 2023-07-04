package com.diu.mlab.foodie.runner.presentation.main

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.FragmentManager
import com.diu.mlab.foodie.runner.R
import com.diu.mlab.foodie.runner.databinding.ActivityRegistrationBinding
import com.diu.mlab.foodie.runner.databinding.ActivityRunnerMainBinding
import com.diu.mlab.foodie.runner.util.changeStatusBarColor
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RunnerMainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRunnerMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRunnerMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
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
                        .replace(binding.mainFragment.id, DeliveryFragment.newInstance("old"))
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
}