package com.diu.mlab.foodie.runner.presentation.main

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.diu.mlab.foodie.runner.databinding.ActivityOrderInfoBinding
import com.diu.mlab.foodie.runner.domain.model.OrderInfo
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OrderInfoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOrderInfoBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bundle = intent.extras
        val orderId = bundle?.getString("orderId") ?: " "
        val path = bundle?.getString("path") ?: " "
        var orderInfo = OrderInfo()
    }
}