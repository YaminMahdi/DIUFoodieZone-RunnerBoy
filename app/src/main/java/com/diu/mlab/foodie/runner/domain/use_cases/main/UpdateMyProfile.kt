package com.diu.mlab.foodie.runner.domain.use_cases.main


import com.diu.mlab.foodie.runner.domain.model.FoodieUser
import com.diu.mlab.foodie.runner.domain.repo.MainRepo
import javax.inject.Inject

class UpdateMyProfile @Inject constructor(
    private val repo: MainRepo
) {

    operator fun invoke(
        runner: FoodieUser,
        picUpdated: Boolean,
        success: () -> Unit,
        failed: (msg: String) -> Unit
    ) {
        if(runner.nm.isEmpty())
            failed.invoke("You must add Name.")
        else if(runner.phone.isEmpty())
            failed.invoke("You must add Phone Number.")
        else if(runner.userType.isEmpty())
            failed.invoke("You must add Runner Type.")
        else if(runner.pic.isEmpty())
            failed.invoke("You must add Profile Photo.")
        else
            repo.updateMyProfile(runner, picUpdated, success, failed)
    }
}