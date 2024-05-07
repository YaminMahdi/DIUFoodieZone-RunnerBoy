package com.diu.mlab.foodie.runner.data.repo

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import com.diu.mlab.foodie.runner.domain.model.FoodieUser
import com.diu.mlab.foodie.runner.domain.model.OrderInfo
import com.diu.mlab.foodie.runner.domain.repo.MainRepo
import com.diu.mlab.foodie.runner.util.copyUriToFile
import com.diu.mlab.foodie.runner.util.transformedEmailId
import com.google.firebase.auth.auth
import com.google.firebase.database.*
import com.google.firebase.database.getValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Firebase
import com.google.firebase.storage.FirebaseStorage
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.default
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@OptIn(DelicateCoroutinesApi::class)
class MainRepoImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val realtime: FirebaseDatabase,
    private val storage: FirebaseStorage,
    private val context: Context
) : MainRepo{

    override fun getMyProfile(
        success: (runner: FoodieUser) -> Unit,
        failed: (msg: String) -> Unit
    ){
        val runnerEmail : String = Firebase.auth.currentUser?.email?.transformedEmailId() ?: "nai"

        firestore.collection("runnerProfiles").document(runnerEmail)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    Log.d("TAG", "DocumentSnapshot data: ${document.data}")
                    val runner = document.toObject(FoodieUser::class.java)!!
                    success.invoke(runner)
                } else {
                    Log.d("TAG", "No such document")
                    failed.invoke("User doesn't exist")
                }
            }
            .addOnFailureListener { exception ->
                Log.d("TAG", "get failed with ", exception)
                failed.invoke("Something went wrong")

            }
    }

    override fun updateMyProfile(
        runner: FoodieUser,
        picUpdated: Boolean,
        success: () -> Unit,
        failed: (msg: String) -> Unit
    ) {
        val runnerEmail : String = Firebase.auth.currentUser?.email?.transformedEmailId() ?: "nai"

        val shopRef = storage.reference.child("runner/${runnerEmail}")
        val path = firestore.collection("runnerProfiles").document(runnerEmail)

        var tmpRunnerInfo = runner

        GlobalScope.launch(Dispatchers.IO){
            if(picUpdated) {
                var logo = context.copyUriToFile(Uri.parse(runner.pic))
                logo = Compressor.compress(context, logo) {
                    default(height = 360, width = 360, format = Bitmap.CompressFormat.JPEG)
                }
                val logoLink = shopRef.child("logo.jpg")
                    .putFile(Uri.fromFile(logo)).await().storage.downloadUrl.await()
                tmpRunnerInfo = runner.copy(pic = logoLink.toString())
            }
            path.set(tmpRunnerInfo)
                .addOnSuccessListener {
                    Log.d("TAG", "DocumentSnapshot successfully written!")
                    success.invoke()
                }
                .addOnFailureListener { exception ->
                    Log.d("TAG", "get failed with ", exception)
                    failed.invoke("Something went wrong")
                }

        }
    }

    override fun getCurrentOrderList(
        path: String, //accepted, available
        success: (orderInfoList: List<OrderInfo>) -> Unit,
        failed: (msg: String) -> Unit
    ) {
        val runnerEmail : String = Firebase.auth.currentUser?.email?.transformedEmailId() ?: "nai"

        val myOrderList = mutableListOf<OrderInfo>()
        val ref = realtime.getReference("orderInfo/current")

        ref.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {

                val info = snapshot.getValue<OrderInfo>()!!
                if(path == "accepted" && info.isRunnerChosen && info.runnerInfo.email == runnerEmail){
                    if(info.isFoodHandover2User && info.foodHandover2UserTime != 0L){
                        ref.child(info.orderId).removeValue()
                        realtime.getReference("orderInfo/runner")
                            .child(runnerEmail)
                            .child(info.orderId)
                            .setValue(info)
                    }
                    else {
                        myOrderList.add(0,info)
                        success.invoke(myOrderList)
                    }
                }
                else if(path == "available" && !info.isRunnerChosen && info.isPaymentConfirmed){
                    myOrderList.add(0,info)
                    success.invoke(myOrderList)
                }
            }

            override fun onChildChanged(
                snapshot: DataSnapshot,
                previousChildName: String?
            ) {
                val info = snapshot.getValue<OrderInfo>()!!
                myOrderList.forEachIndexed { index, orderInfo ->
                        if(
                            orderInfo.orderId == previousChildName &&
                            path == "available" &&
                            info.isRunnerChosen
                        ){
                            success.invoke(myOrderList.toMutableList().apply { removeAt(index) })
                        }else{
                            success.invoke(emptyList())
                        }
                    }
            }
            override fun onChildRemoved(snapshot: DataSnapshot) {
                val info = snapshot.getValue<OrderInfo>()!!
                success.invoke(myOrderList.toMutableList().apply { remove(info) })
            }
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {
                failed.invoke(error.message)
            }
        })    }

    override fun getMyCompletedOrderList(
        success: (orderInfoList: List<OrderInfo>) -> Unit,
        failed: (msg: String) -> Unit
    ) {
        val runnerEmail : String = Firebase.auth.currentUser?.email?.transformedEmailId() ?: "nai"

        val myOrderList = mutableListOf<OrderInfo>()
        val ref = realtime.getReference("orderInfo/runner").child(runnerEmail)

        ref.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val info = snapshot.getValue<OrderInfo>()!!
                myOrderList.add(0,info)
                success.invoke(myOrderList)
            }
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {
                failed.invoke(error.message)
            }
        })
    }

    override fun getOrderInfo(
        orderId: String,
        path: String, //current, completed
        success: (orderInfo: OrderInfo) -> Unit,
        failed: (msg: String) -> Unit
    ) {
        val runnerEmail : String = Firebase.auth.currentUser?.email?.transformedEmailId() ?: "nai"

        val ref =
            if(path == "completed") realtime.getReference("orderInfo/runner").child(runnerEmail).child(orderId)
            else realtime.getReference("orderInfo/current").child(orderId)

        ref.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val info = snapshot.getValue<OrderInfo>()
                    success.invoke(info ?: OrderInfo())
                }

                override fun onCancelled(error: DatabaseError) {
                    failed.invoke(error.message)
                }
            })
    }

    override fun acceptOrderDelivery(
        orderInfo: OrderInfo,
        success: () -> Unit,
        failed: (msg: String) -> Unit
    ) {
        val userEmail = orderInfo.userInfo.email
        val shopEmail = orderInfo.shopInfo.email
        getMyProfile({
            val updatedOrderInfo = orderInfo.copy(
                runnerInfo = it,
                isRunnerChosen = true,
                runnerChosenTime = System.currentTimeMillis()
            )

            realtime
                .getReference("orderInfo/all")
                .child(userEmail)
                .child(orderInfo.orderId)
                .setValue(updatedOrderInfo)

            realtime
                .getReference("orderInfo/current")
                .child(orderInfo.orderId)
                .setValue(updatedOrderInfo)
                .addOnSuccessListener {
                    success.invoke()
                }
                .addOnFailureListener {ex->
                    failed.invoke(ex.message.toString())
                }
            realtime
                .getReference("orderInfo/shop")
                .child(shopEmail)
                .child("current")
                .child(orderInfo.orderId)
                .setValue(updatedOrderInfo)
        },failed)
    }

    override fun updateOrderInfo(
        orderId: String,
        varBoolName: String,
        value: Boolean,
        varTimeName: String,
        userEmail: String,
        shopEmail: String,
        success: () -> Unit,
        failed: (msg: String) -> Unit
    ) {
        realtime
            .getReference("orderInfo/all")
            .child(userEmail)
            .child(orderId)
            .child(varBoolName)
            .setValue(value)

        realtime
            .getReference("orderInfo/current")
            .child(orderId)
            .child(varBoolName)
            .setValue(value)
            .addOnSuccessListener {
                success.invoke()
            }
            .addOnFailureListener {
                failed.invoke(it.message.toString())
            }
        realtime
            .getReference("orderInfo/shop")
            .child(shopEmail)
            .child("current")
            .child(orderId)
            .child(varBoolName)
            .setValue(value)

        val time = System.currentTimeMillis()
        realtime
            .getReference("orderInfo/all")
            .child(userEmail)
            .child(orderId)
            .child(varTimeName)
            .setValue(time)
        realtime
            .getReference("orderInfo/current")
            .child(orderId)
            .child(varTimeName)
            .setValue(time)
        realtime
            .getReference("orderInfo/shop")
            .child(shopEmail)
            .child("current")
            .child(orderId)
            .child(varTimeName)
            .setValue(time)
    }
}