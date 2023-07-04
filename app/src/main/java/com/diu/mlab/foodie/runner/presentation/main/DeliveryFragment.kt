package com.diu.mlab.foodie.runner.presentation.main

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import com.diu.mlab.foodie.runner.databinding.FragmentDeliveryBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DeliveryFragment : Fragment() {
    private var type: String = "current"
    private val viewModel : MainViewModel by viewModels()
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


        return binding.root
    }


}