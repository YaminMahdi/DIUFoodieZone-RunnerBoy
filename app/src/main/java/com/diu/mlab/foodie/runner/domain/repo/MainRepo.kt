package com.diu.mlab.foodie.runner.domain.repo

import com.diu.mlab.foodie.runner.domain.model.FoodieUser
import com.diu.mlab.foodie.runner.domain.model.OrderInfo

interface MainRepo {

    fun getMyProfile(success :(runner: FoodieUser) -> Unit, failed :(msg : String) -> Unit)

    fun updateMyProfile(runner: FoodieUser, picUpdated: Boolean, success :() -> Unit, failed :(msg : String) -> Unit)

    fun getCurrentOrderList(path: String, success :(orderInfoList: List<OrderInfo>) -> Unit, failed :(msg : String) -> Unit)

    fun getMyCompletedOrderList(success :(orderInfoList: List<OrderInfo>) -> Unit, failed :(msg : String) -> Unit)

    fun getOrderInfo(orderId: String, path: String, success :(orderInfo: OrderInfo) -> Unit, failed :(msg : String) -> Unit)

    fun acceptOrderDelivery(orderInfo: OrderInfo, success :() -> Unit, failed :(msg : String) -> Unit)

    fun updateOrderInfo(
        orderId: String,
        varBoolName: String,
        value: Boolean,
        varTimeName: String,
        userEmail: String,
        shopEmail: String,
        success :() -> Unit,
        failed :(msg : String) -> Unit
    )


}