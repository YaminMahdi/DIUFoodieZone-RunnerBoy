package com.diu.mlab.foodie.runner.presentation.auth

import android.app.Activity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diu.mlab.foodie.runner.domain.use_cases.auth.AuthUseCases
import com.diu.mlab.foodie.runner.domain.model.FoodieUser
import com.google.android.gms.auth.api.identity.SignInCredential
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authUseCases: AuthUseCases,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
//    val superUser= savedStateHandle.getLiveData<SuperUser>("superUser")

    fun googleSignIn(
        activity: Activity,
        resultLauncher : ActivityResultLauncher<IntentSenderRequest>,
        runner: FoodieUser? =null,
        failed : (msg : String) -> Unit,
        ){
        viewModelScope.launch(Dispatchers.IO)
        {
            authUseCases.googleSignIn(runner,activity, resultLauncher, failed)
        }
    }

    fun firebaseSignup(
        credential: SignInCredential,runner: FoodieUser, success :() -> Unit, failed :(msg : String) -> Unit
    ){
        viewModelScope.launch(Dispatchers.IO){
            authUseCases.firebaseSignup(credential, runner, success, failed)
        }
    }

    fun firebaseLogin(
        credential: SignInCredential, success :(runner: FoodieUser) -> Unit, failed :(msg : String) -> Unit
    ){
        viewModelScope.launch(Dispatchers.IO)
        {
            authUseCases.firebaseLogin(credential, success, failed)
        }
    }
}