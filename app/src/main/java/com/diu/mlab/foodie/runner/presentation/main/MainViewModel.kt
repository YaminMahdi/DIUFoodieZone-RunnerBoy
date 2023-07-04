package com.diu.mlab.foodie.runner.presentation.main

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.diu.mlab.foodie.runner.domain.model.FoodieUser
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    val myProfile = savedStateHandle.getLiveData("myProfile",FoodieUser())

}
