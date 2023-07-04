package com.diu.mlab.foodie.runner.domain.use_cases.main


import com.diu.mlab.foodie.runner.domain.model.FoodieUser
import com.diu.mlab.foodie.runner.domain.model.OrderInfo
import com.diu.mlab.foodie.runner.domain.repo.MainRepo
import javax.inject.Inject

class GetCurrentOrderList @Inject constructor(
    private val repo: MainRepo
) {

    operator fun invoke(
        path: String, success :(orderInfoList: List<OrderInfo>) -> Unit, failed :(msg : String) -> Unit
    ) = repo.getCurrentOrderList(path, success, failed)
}