package com.diu.mlab.foodie.runner.presentation.main

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.diu.mlab.foodie.runner.databinding.ItemRequestInfoBinding
import com.diu.mlab.foodie.runner.domain.model.OrderInfo
import com.diu.mlab.foodie.runner.util.loadDrawable
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class RequestListViewAdapter(
    private val list: List<OrderInfo>,
    private val viewModel: MainViewModel
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class ViewHolder(
        private val binding: ItemRequestInfoBinding,
        private val contest: Context,
        private val viewModel: MainViewModel,
        ) : RecyclerView.ViewHolder(binding.root) {
        fun bindView(
            list: List<OrderInfo>,
            position: Int
        ) {
            binding.shopNm.text = list[position].shopInfo.nm
            binding.shopAddress.text = list[position].shopInfo.loc

            binding.userNm.text = list[position].userInfo.nm.split("(?<=\\D)(?=\\d)".toRegex())[0]
            binding.userType.text = list[position].userInfo.userType
            binding.userAddress.text = list[position].userInfo.loc


            binding.foodNm.text = list[position].foodInfo.nm
            binding.time.text = list[position].foodInfo.time
            binding.deliveryCharge.text = list[position].deliveryCharge.toString()
            binding.pic.loadDrawable(list[position].foodInfo.pic)

            binding.btnAcceptDelivery.setOnClickListener {
                viewModel.acceptOrderDelivery(list[position],{},{
                    MainScope().launch {
                        Toast.makeText(contest, it, Toast.LENGTH_SHORT).show()
                    }
                })
                viewModel.setCurrentOrderList(list.toMutableList().apply { removeAt(position) })
            }
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        ViewHolder(
            ItemRequestInfoBinding.inflate(
                LayoutInflater.from(viewGroup.context),
                viewGroup, false), viewGroup.context, viewModel
        )

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ViewHolder).bindView(list, position)
    }
}
