package com.diu.mlab.foodie.runner.domain.use_cases.auth

import android.app.Activity
import com.diu.mlab.foodie.runner.domain.RequestState
import com.diu.mlab.foodie.runner.domain.model.FoodieUser
import com.diu.mlab.foodie.runner.domain.repo.AuthRepo
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import javax.inject.Inject

class GoogleSignIn @Inject constructor (
    val repo: AuthRepo
) {
    suspend operator fun invoke(
        activity: Activity,
        runner: FoodieUser?,
    ): RequestState<GoogleIdTokenCredential> {
        return if(runner != null){
            if(runner.nm.isEmpty())
                RequestState.Error("You must add Name.")
            else if(runner.phone.isEmpty())
                RequestState.Error("You must add Phone Number.")
            else if(runner.userType.isEmpty())
                RequestState.Error("You must add Runner Type.")
            else if(runner.pic.isEmpty())
                RequestState.Error("You must add Profile Photo.")
            else
                repo.googleSignIn(activity, false)
        }
        else //login
            repo.googleSignIn(activity, true)
    }
}