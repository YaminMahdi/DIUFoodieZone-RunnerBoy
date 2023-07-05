package com.diu.mlab.foodie.runner.domain.use_cases.main

import javax.inject.Inject

data class MainUseCases @Inject constructor(
    val getMyProfile: GetMyProfile,
    val updateMyProfile: UpdateMyProfile,
    val getCurrentOrderList: GetCurrentOrderList,
    val getMyCompletedOrderList: GetMyCompletedOrderList,
    val getOrderInfo: GetOrderInfo,
    val acceptOrderDelivery: AcceptOrderDelivery,
    val updateOrderInfo: UpdateOrderInfo
)
