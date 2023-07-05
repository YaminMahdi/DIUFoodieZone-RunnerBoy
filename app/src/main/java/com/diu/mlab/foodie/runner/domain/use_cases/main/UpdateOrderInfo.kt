package com.diu.mlab.foodie.runner.domain.use_cases.main


import com.diu.mlab.foodie.runner.domain.model.FoodieUser
import com.diu.mlab.foodie.runner.domain.repo.MainRepo
import javax.inject.Inject

class UpdateOrderInfo @Inject constructor(
    private val repo: MainRepo
) {

    operator fun invoke(
        orderId: String,
        varBoolName: String,
        value: Boolean,
        varTimeName: String,
        userEmail: String,
        shopEmail: String,
        success :() -> Unit,
        failed :(msg : String) -> Unit
    ) {
        if(userEmail.isEmpty() || shopEmail.isEmpty())
            failed.invoke("Something went wrong.")
        else
            repo.updateOrderInfo(orderId, varBoolName, value, varTimeName, userEmail, shopEmail, success, failed)
    }
}