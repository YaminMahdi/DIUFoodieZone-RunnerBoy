package com.diu.mlab.foodie.runner.domain.use_cases.main


import com.diu.mlab.foodie.runner.domain.model.FoodieUser
import com.diu.mlab.foodie.runner.domain.repo.MainRepo
import javax.inject.Inject

class GetMyProfile @Inject constructor(
    private val repo: MainRepo
) {

    operator fun invoke(
        success :(runner: FoodieUser) -> Unit, failed :(msg : String) -> Unit
    ) = repo.getMyProfile(success, failed)
}