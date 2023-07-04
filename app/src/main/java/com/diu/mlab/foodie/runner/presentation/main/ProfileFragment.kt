package com.diu.mlab.foodie.runner.presentation.main

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.diu.mlab.foodie.runner.R
import com.diu.mlab.foodie.runner.databinding.FragmentDeliveryBinding
import com.diu.mlab.foodie.runner.databinding.FragmentProfileBinding
import com.diu.mlab.foodie.runner.presentation.auth.LoginActivity
import com.diu.mlab.foodie.runner.util.setBounceClickListener
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileFragment : Fragment() {
    private val viewModel : MainViewModel by viewModels()
    private lateinit var binding: FragmentProfileBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)

        binding.edit.setBounceClickListener{
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.run {
                addToBackStack("xyz")
                hide(this@ProfileFragment)
                add(R.id.mainFragment, ProfileFragment())
                commit()
            }
        }
        binding.logout.setBounceClickListener{
            Firebase.auth.signOut()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            activity?.finish()
        }

        return binding.root
    }

}