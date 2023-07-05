package com.diu.mlab.foodie.runner.presentation.main

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.diu.mlab.foodie.runner.R
import com.diu.mlab.foodie.runner.databinding.ItemOrderBinding
import com.diu.mlab.foodie.runner.domain.model.OrderInfo
import com.diu.mlab.foodie.runner.util.getDrawable

class OrderListViewAdapter(
    private val list: List<OrderInfo>,
    private val path: String
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class ViewHolder(
        private val binding: ItemOrderBinding,
        private val contest: Context,
        private val path: String
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bindView(
            list: List<OrderInfo>,
            position: Int
        ) {
            binding.nm.text = "${list[position].foodInfo.nm} ${list[position].type} ×${list[position].quantity}"
            binding.shopAddress.text = "${list[position].shopInfo.nm}, ${list[position].shopInfo.loc}"

            binding.userNm.text = list[position].userInfo.nm.split("(?<=\\D)(?=\\d)".toRegex())[0]
            binding.userType.text = list[position].userInfo.userType
            binding.userAddress.text = list[position].userInfo.loc


            binding.deliveryCharge.text = list[position].deliveryCharge.toString()
            list[position].foodInfo.pic.getDrawable{ binding.pic.setImageDrawable(it) }



            if(list[position].isUserReceived)
                binding.orderStatusTxt.text = "Received"
            else if(!list[position].isUserReceived && list[position].userReceivedTime != 0L)
                binding.orderStatusTxt.text = "Not Received"
            else if(list[position].isFoodHandover2User)
                binding.orderStatusTxt.text = "Delivered"
            else if(list[position].isFoodHandover2RunnerNdPaid)
                binding.orderStatusTxt.text = "On The Way"
            else if(list[position].isCanceled)
                binding.orderStatusTxt.text = "Canceled"
            else if(list[position].isRunnerChosen)
                binding.orderStatusTxt.text = "Accepted"
            else
                binding.orderStatusTxt.text = "Processing"



            if((!list[position].isUserReceived && list[position].userReceivedTime != 0L) || list[position].isCanceled)
                binding.orderStatusCard.backgroundTintList = ColorStateList.valueOf(contest.getColor(R.color.redZ))
            if(list[position].isFoodHandover2User || list[position].isUserReceived)
                binding.orderStatusCard.backgroundTintList = ColorStateList.valueOf(contest.getColor(R.color.greenX))


            binding.btnViewOrder.setOnClickListener {
                val bundle = Bundle()
                bundle.putString("orderId", list[position].orderId)
                bundle.putString("path", path)

                contest.startActivity(
                    Intent(contest, OrderInfoActivity::class.java).putExtras(bundle)
                )
            }
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        ViewHolder(
            ItemOrderBinding.inflate(
                LayoutInflater.from(viewGroup.context),
                viewGroup, false), viewGroup.context, path
        )

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ViewHolder).bindView(list, position)
    }
}
