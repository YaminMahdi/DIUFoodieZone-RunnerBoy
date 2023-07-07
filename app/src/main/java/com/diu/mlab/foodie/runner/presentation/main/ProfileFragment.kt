package com.diu.mlab.foodie.runner.presentation.main

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.diu.mlab.foodie.runner.R
import com.diu.mlab.foodie.runner.databinding.FragmentDeliveryBinding
import com.diu.mlab.foodie.runner.databinding.FragmentProfileBinding
import com.diu.mlab.foodie.runner.presentation.auth.LoginActivity
import com.diu.mlab.foodie.runner.util.getDrawable
import com.diu.mlab.foodie.runner.util.setBounceClickListener
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfileFragment : Fragment() {
    private val viewModel : MainViewModel by activityViewModels()
    private lateinit var binding: FragmentProfileBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)

        viewModel.getMyProfile {
            MainScope().launch {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }
        viewModel.getMyCompletedOrderList {
            MainScope().launch {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }
        viewModel.myProfile.observe(viewLifecycleOwner){ runner->
            binding.nm.text = runner.nm
            binding.des.text = runner.userType
            binding.pn.text = runner.phone
            runner.pic.getDrawable { binding.pic.setImageDrawable(it) }
        }
        viewModel.myCompletedOrderList.observe(viewLifecycleOwner){lst->
            val earnings= lst.sumOf { it.deliveryCharge }
            binding.earnings.text = earnings.toString()
        }

        binding.edit.setBounceClickListener{
            startActivity(Intent(requireContext(), ProfileEditActivity::class.java))
        }
        binding.logout.setBounceClickListener{
            Firebase.auth.signOut()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            activity?.finish()
        }

        return binding.root
    }

}