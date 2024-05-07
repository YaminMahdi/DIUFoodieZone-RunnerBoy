package com.diu.mlab.foodie.runner.domain.repo

import android.app.Activity
import com.diu.mlab.foodie.runner.domain.RequestState
import com.diu.mlab.foodie.runner.domain.model.FoodieUser
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

interface AuthRepo {
    fun firebaseLogin(credential: GoogleIdTokenCredential, success :(runner: FoodieUser) -> Unit, failed :(msg : String) -> Unit)
    fun firebaseSignup(credential: GoogleIdTokenCredential, runner: FoodieUser, success :() -> Unit, failed :(msg : String) -> Unit)
    suspend fun googleSignIn(activity: Activity, isAuthorized : Boolean): RequestState<GoogleIdTokenCredential>
}