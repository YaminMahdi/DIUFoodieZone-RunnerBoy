package com.diu.mlab.foodie.runner.domain.use_cases.main

import com.diu.mlab.foodie.runner.domain.model.OrderInfo
import com.diu.mlab.foodie.runner.domain.repo.MainRepo
import javax.inject.Inject

class AcceptOrderDelivery @Inject constructor (
    val repo: MainRepo
) {
    operator fun invoke(
        orderInfo: OrderInfo, success :() -> Unit, failed :(msg : String) -> Unit
    ) {
        if(orderInfo.orderId.isNotEmpty() && orderInfo.isPaymentConfirmed)
            repo.acceptOrderDelivery(orderInfo, success, failed)
        else
            failed.invoke("Something went wrong")
    }

}