package com.diu.mlab.foodie.runner.domain.use_cases.main

import com.diu.mlab.foodie.runner.domain.model.OrderInfo
import com.diu.mlab.foodie.runner.domain.repo.MainRepo
import javax.inject.Inject

class GetOrderInfo @Inject constructor (
    val repo: MainRepo
) {
    operator fun invoke(
        orderId: String, path: String, success :(orderInfo: OrderInfo) -> Unit, failed :(msg : String) -> Unit
    ) {
        if(orderId.isNotEmpty())
            repo.getOrderInfo(orderId, path, success, failed)
        else
            failed.invoke("Something went wrong")
    }

}