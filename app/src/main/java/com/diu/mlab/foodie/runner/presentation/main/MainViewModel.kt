package com.diu.mlab.foodie.runner.presentation.main

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diu.mlab.foodie.runner.domain.model.FoodieUser
import com.diu.mlab.foodie.runner.domain.model.OrderInfo
import com.diu.mlab.foodie.runner.domain.use_cases.main.MainUseCases
import com.diu.mlab.foodie.runner.util.toDateTime
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val mainUseCases: MainUseCases,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    val myProfile = savedStateHandle.getLiveData("myProfile",FoodieUser())
    val orderInfo = savedStateHandle.getLiveData("orderInfo",OrderInfo())

    val currentOrderList = savedStateHandle.getLiveData("currentOrderList", emptyList<OrderInfo>())
    val myCompletedOrderList = savedStateHandle.getLiveData("myCompletedOrderList", emptyList<OrderInfo>())
    val progressInfoList= savedStateHandle.getLiveData("progressInfoList", emptyList<Pair<String, String>>())

    private var orderListJob : Job? =null
    private var orderInfoJob : Job? =null

    fun getMyProfile( failed :(msg : String) -> Unit){
        viewModelScope.launch(Dispatchers.IO){
            mainUseCases.getMyProfile({
                savedStateHandle["myProfile"] = it
            },failed)
        }
    }

    fun updateMyProfile(runner: FoodieUser, picUpdated: Boolean, success :() -> Unit, failed :(msg : String) -> Unit){
        viewModelScope.launch(Dispatchers.IO){
            mainUseCases.updateMyProfile(runner,picUpdated,{
                getMyProfile(failed)
                success.invoke()
            },failed)
        }
    }

    fun getCurrentOrderList(path: String, failed :(msg : String) -> Unit){
        orderListJob?.apply {
            cancel()
            savedStateHandle["currentOrderList"] = emptyList<List<OrderInfo>>()
        }
        orderListJob = viewModelScope.launch(Dispatchers.IO){
            mainUseCases.getCurrentOrderList(path,{
                savedStateHandle["currentOrderList"] = it
            },failed)
        }
    }

    fun setCurrentOrderList(list: List<OrderInfo>){
        savedStateHandle["currentOrderList"] = list
    }

    fun getMyCompletedOrderList(failed :(msg : String) -> Unit){
        viewModelScope.launch(Dispatchers.IO){
            mainUseCases.getMyCompletedOrderList({
                savedStateHandle["myCompletedOrderList"] = it
            },failed)
        }
    }

    fun getOrderInfo(orderId: String, path: String, failed :(msg : String) -> Unit){
        orderInfoJob?.apply {
            cancel()
            savedStateHandle["orderInfo"] = OrderInfo()
        }
        orderInfoJob = viewModelScope.launch(Dispatchers.IO){
            mainUseCases.getOrderInfo(orderId,path,{odrInfo->
                val tmpList = mutableListOf<Pair<String,String>>()

                savedStateHandle["orderInfo"] = odrInfo
                if(odrInfo.isRunnerChosen)
                    tmpList.add(Pair("Runner Chosen", odrInfo.runnerChosenTime.toDateTime()))
                if(odrInfo.isFoodHandover2RunnerNdPaid)
                    tmpList.add(Pair("Food Given To Runner", odrInfo.foodHandover2RunnerTime.toDateTime()))
                if(odrInfo.runnerReceivedTime != 0L){
                    if(odrInfo.isRunnerReceivedFoodnMoney)
                        tmpList.add(Pair("Runner Got Paid", odrInfo.runnerReceivedTime.toDateTime()))
                    else
                        tmpList.add(Pair("Runner Didn't Got Paid", odrInfo.runnerReceivedTime.toDateTime()))
                }
                if(odrInfo.isFoodHandover2User)
                    tmpList.add(Pair("Food Delivered", odrInfo.foodHandover2UserTime.toDateTime()))
                if(odrInfo.userReceivedTime != 0L) {
                    if(odrInfo.isUserReceived)
                        tmpList.add(Pair("Food Received", odrInfo.userReceivedTime.toDateTime()))
                    else
                        tmpList.add(Pair("Food Not Received", odrInfo.userReceivedTime.toDateTime()))
                }
                if(odrInfo.isCanceled)
                    tmpList.add(Pair("Canceled", odrInfo.canceledTime.toDateTime()))

                savedStateHandle["progressInfoList"]= tmpList
            },failed)
        }
    }

    fun acceptOrderDelivery(orderInfo: OrderInfo, success :() -> Unit, failed :(msg : String) -> Unit){
        viewModelScope.launch(Dispatchers.IO){
            mainUseCases.acceptOrderDelivery(orderInfo,success,failed)
        }
    }

    fun updateOrderInfo(
        orderId: String,
        varBoolName: String,
        value: Boolean,
        varTimeName: String,
        userEmail: String,
        shopEmail: String,
        success :() -> Unit,
        failed :(msg : String) -> Unit
    ){
        viewModelScope.launch(Dispatchers.IO){
            mainUseCases.updateOrderInfo(orderId, varBoolName, value, varTimeName, userEmail, shopEmail, success, failed)
        }
    }
}
