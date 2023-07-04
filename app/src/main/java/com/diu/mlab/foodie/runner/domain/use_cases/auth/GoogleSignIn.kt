package com.diu.mlab.foodie.runner.domain.use_cases.auth

import android.app.Activity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.diu.mlab.foodie.runner.domain.model.FoodieUser
import com.diu.mlab.foodie.runner.domain.repo.AuthRepo
import javax.inject.Inject

class GoogleSignIn @Inject constructor (
    val repo: AuthRepo
) {
    operator fun invoke(
        runner: FoodieUser?,
        activity: Activity,
        resultLauncher : ActivityResultLauncher<IntentSenderRequest>,
        failed :(msg : String) -> Unit
    ) {
        if(runner != null){
            if(runner.nm.isEmpty())
                failed.invoke("You must add Name.")
            else if(runner.phone.isEmpty())
                failed.invoke("You must add Phone Number.")
            else if(runner.userType.isEmpty())
                failed.invoke("You must add Runner Type.")
            else if(runner.pic.isEmpty())
                failed.invoke("You must add Profile Photo.")
            else
                repo.googleSignIn(activity, resultLauncher, failed)
        }
        else
            repo.googleSignIn(activity, resultLauncher, failed)

    }
}