package com.diu.mlab.foodie.runner.presentation.auth

import android.app.Activity
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diu.mlab.foodie.runner.domain.RequestState
import com.diu.mlab.foodie.runner.domain.model.FoodieUser
import com.diu.mlab.foodie.runner.domain.use_cases.auth.AuthUseCases
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authUseCases: AuthUseCases,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

val loadingVisibility= savedStateHandle.getLiveData<Boolean>("loading")

    fun googleSignIn(
        activity: Activity,
        runner: FoodieUser? =null,
        result : (msg : RequestState<GoogleIdTokenCredential>) -> Unit,
        ){
        setLoadingVisibility(true)
        viewModelScope.launch {
            val data = authUseCases.googleSignIn(activity, runner)
            result.invoke(data)
            if(data is RequestState.Error)
                setLoadingVisibility(false, if (data.code==20) 0 else 1)
        }
    }

    fun firebaseSignup(
        credential: GoogleIdTokenCredential,runner: FoodieUser, success :() -> Unit, failed :(msg : String) -> Unit
    ){
        viewModelScope.launch(Dispatchers.IO){
            authUseCases.firebaseSignup(credential, runner, success){
                setLoadingVisibility(false)
                failed.invoke(it)
            }
        }
    }

    fun firebaseLogin(
        credential: GoogleIdTokenCredential, success :(runner: FoodieUser) -> Unit, failed :(msg : String) -> Unit
    ){
        viewModelScope.launch(Dispatchers.IO) {
            authUseCases.firebaseLogin(credential, success){
                setLoadingVisibility(false)
                failed.invoke(it)
            }
        }
    }

    private fun setLoadingVisibility(visibility: Boolean, time: Int = 1){
        viewModelScope.launch(Dispatchers.Main){
            if(!visibility)
                delay(time.seconds)
            savedStateHandle["loading"] = visibility
            Log.d("TAG", "setLoadingVisibility: $visibility")
        }
    }

}