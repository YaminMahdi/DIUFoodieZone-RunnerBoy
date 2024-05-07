package com.diu.mlab.foodie.runner.presentation.main

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import com.diu.mlab.foodie.runner.databinding.FragmentDeliveryBinding
import com.jem.fliptabs.FlipTab
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DeliveryFragment : Fragment() {
    private var type: String = "current"
    private val viewModel : MainViewModel by activityViewModels()
    private lateinit var binding: FragmentDeliveryBinding

    companion object {
        @JvmStatic
        fun newInstance(type: String) =
            DeliveryFragment().apply {
                arguments = bundleOf("type" to type)
            }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            type = it.getString("type")!!
            Log.d("TAG", "onCreate: $type")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDeliveryBinding.inflate(inflater, container, false)
        if(type == "completed"){
            binding.flipTab.visibility = View.GONE
            viewModel.getMyCompletedOrderList{
                MainScope().launch{
                    Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                }
            }
        }
        else{
            viewModel.getCurrentOrderList(viewModel.path) {
                MainScope().launch{
                    Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                }
            }
        }
        viewModel.currentOrderList.observe(viewLifecycleOwner){
            when (viewModel.path) {
                "available" -> binding.deliveryListRecyclerView.adapter = RequestListViewAdapter(it, viewModel)
                "accepted" -> binding.deliveryListRecyclerView.adapter = OrderListViewAdapter(it, type)
            }
        }
        viewModel.myCompletedOrderList.observe(viewLifecycleOwner){
            if(type == "completed")
                binding.deliveryListRecyclerView.adapter = OrderListViewAdapter(it, type)
        }

        binding.flipTab.setTabSelectedListener(object: FlipTab.TabSelectedListener {
            override fun onTabSelected(isLeftTab: Boolean, tabTextValue: String) {
                viewModel.path = if (isLeftTab) "available" else  "accepted"
                viewModel.getCurrentOrderList(viewModel.path) {
                    MainScope().launch{
                        Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                    }
                }
            }
            override fun onTabReselected(isLeftTab: Boolean, tabTextValue: String) {}
        })

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        when (viewModel.path) {
            "available" -> binding.flipTab.selectLeftTab(false)
            "accepted" -> binding.flipTab.selectRightTab(false)
        }
    }


}