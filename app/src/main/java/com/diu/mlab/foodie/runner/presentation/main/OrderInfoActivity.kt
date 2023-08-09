package com.diu.mlab.foodie.runner.presentation.main

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import com.diu.mlab.foodie.runner.databinding.ActivityOrderInfoBinding
import com.diu.mlab.foodie.runner.domain.model.OrderInfo
import com.diu.mlab.foodie.runner.util.getDrawable
import com.diu.mlab.foodie.runner.util.setBounceClickListener
import com.diu.mlab.foodie.runner.util.toDateTime
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

@AndroidEntryPoint
class OrderInfoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOrderInfoBinding
    private val viewModel : MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bundle = intent.extras
        val orderId = bundle?.getString("orderId") ?: " "
        val type = bundle?.getString("type") ?: " "
        var orderInfo = OrderInfo()

        viewModel.getOrderInfo(orderId, type){
            MainScope().launch {
                Toast.makeText(this@OrderInfoActivity, it, Toast.LENGTH_SHORT).show()
            }
        }
        viewModel.orderInfo.observe(this){ info ->
            if(info.foodInfo.nm.length < orderInfo.foodInfo.nm.length) {
                Toast.makeText(this@OrderInfoActivity, "Food Delivered Successfully", Toast.LENGTH_SHORT).show()
                onBackPressedDispatcher.onBackPressed()
            }
            orderInfo = info
            binding.foodNm.text = info.foodInfo.nm
            if(info.foodInfo.types.isNotEmpty()){
                binding.foodType.text = info.type
                binding.typeLayout.visibility = View.VISIBLE
            }
            binding.quantity.text = info.quantity.toString()
            binding.shopLoc.text = info.shopInfo.loc
            binding.userLoc.text = info.userInfo.loc
            binding.deliveryCharge.text = "${info.deliveryCharge}"
            binding.deliveryTime.text = info.foodInfo.time
            binding.orderTime.text = info.orderTime.toDateTime()

            info.userInfo.pic.getDrawable { binding.cusPic.setImageDrawable(it) }
            binding.cusNm.text = info.userInfo.nm
            binding.cusDes.text = info.userInfo.userType
            binding.cusPn.text = info.userInfo.phone

            info.shopInfo.pic.getDrawable { binding.shopPic.setImageDrawable(it) }
            binding.shopNm.text = info.shopInfo.nm
            binding.shopLoc.text = info.shopInfo.loc
            binding.shopLoc2.text = info.shopInfo.loc
            binding.shopPn.text = info.shopInfo.phone

            if(
                (info.isFoodHandover2RunnerNdPaid && info.runnerReceivedTime == 0L) ||
                (info.runnerReceivedTime != 0L && !info.isFoodHandover2User)
            )
                binding.foodConfirmation.visibility = View.VISIBLE
            else
                binding.foodConfirmation.visibility = View.GONE

            if(info.isFoodHandover2RunnerNdPaid && info.isRunnerReceivedFoodnMoney){
                binding.confirmationText.text = "Did you handover the food to Customer?"
                binding.btnNo.visibility = View.GONE
                binding.btnYes.text= "Mark As Done"
            }
        }

        viewModel.progressInfoList.observe(this){lst->
            binding.precessRecyclerView.adapter = ProgressListViewAdapter(lst)

            if( lst.map{it.first}.contains("Canceled") || lst.map{it.first}.contains("Food Received"))
                binding.processingBar.visibility = View.INVISIBLE
        }

        binding.btnEdit.setBounceClickListener {
            when(binding.foodConfirmation.visibility){
                View.GONE -> binding.foodConfirmation.visibility = View.VISIBLE
                View.VISIBLE -> binding.foodConfirmation.visibility = View.GONE
            }
        }

        binding.btnYes.setBounceClickListener {
            if(!orderInfo.isFoodHandover2User && orderInfo.isRunnerReceivedFoodnMoney) {
                viewModel.updateOrderInfo(
                    orderId = orderId,
                    varBoolName = "foodHandover2User",
                    value = true,
                    varTimeName = "foodHandover2UserTime",
                    userEmail = orderInfo.userInfo.email,
                    shopEmail = orderInfo.shopInfo.email,
                    success = {},
                    failed = {}
                )
            }
            else{
                viewModel.updateOrderInfo(
                    orderId = orderId,
                    varBoolName = "runnerReceivedFoodnMoney",
                    value = true,
                    varTimeName = "runnerReceivedTime",
                    userEmail = orderInfo.userInfo.email,
                    shopEmail = orderInfo.shopInfo.email,
                    success = {},
                    failed = {}
                )
        }
            binding.foodConfirmation.visibility = View.GONE
        }

        binding.btnNo.setBounceClickListener {
            if(!orderInfo.isRunnerChosen) {
                viewModel.updateOrderInfo(
                    orderId = orderId,
                    varBoolName = "runnerReceivedFoodnMoney",
                    value = false,
                    varTimeName = "runnerReceivedTime",
                    userEmail = orderInfo.userInfo.email,
                    shopEmail = orderInfo.shopInfo.email,
                    success = {},
                    failed = {}
                )
            }
            binding.foodConfirmation.visibility = View.GONE
        }

        binding.shopPn.setBounceClickListener {
            val callIntent = Intent(Intent.ACTION_DIAL )
            callIntent.data = Uri.parse("tel:" + orderInfo.shopInfo.phone) //change the number
            startActivity(callIntent)
        }
        binding.cusPn.setBounceClickListener {
            val callIntent = Intent(Intent.ACTION_DIAL )
            callIntent.data = Uri.parse("tel:" + orderInfo.runnerInfo.phone) //change the number
            startActivity(callIntent)
        }
        binding.btnBack.setBounceClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }
}