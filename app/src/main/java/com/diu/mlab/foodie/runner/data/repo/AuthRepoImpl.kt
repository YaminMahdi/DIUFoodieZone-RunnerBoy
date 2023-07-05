package com.diu.mlab.foodie.runner.data.repo

import android.app.Activity
import android.content.Context
import android.content.IntentSender
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.diu.mlab.foodie.runner.R
import com.diu.mlab.foodie.runner.domain.model.FoodieUser
import com.diu.mlab.foodie.runner.domain.repo.AuthRepo
import com.diu.mlab.foodie.runner.util.copyUriToFile
import com.diu.mlab.foodie.runner.util.transformedEmailId
import com.google.android.gms.auth.api.identity.GetSignInIntentRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.default
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject


class AuthRepoImpl @Inject constructor(
    private val auth : FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val context: Context
    ) : AuthRepo {


    override fun firebaseLogin(
        credential: SignInCredential,
        success: (runner: FoodieUser) -> Unit,
        failed: (msg: String) -> Unit
    ) {
        val authCredential = GoogleAuthProvider.getCredential(credential.googleIdToken, null)
        auth.signInWithCredential(authCredential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("TAG", "signInWithCredential:success")
                    firestore.collection("runnerProfiles").document(credential.id.transformedEmailId())
                        .get()
                        .addOnSuccessListener { document ->
                            if (document != null && document.exists()) {
                                Log.d("TAG", "DocumentSnapshot data: ${document.data}")
                                val superUser = document.toObject(FoodieUser::class.java)!!
                                success.invoke(superUser)
                            } else {
                                Log.d("TAG", "No such document")
                                failed.invoke("User doesn't exist")
                            }
                        }
                        .addOnFailureListener { exception ->
                            Log.d("TAG", "get failed with ", exception)
                            failed.invoke("Something went wrong")

                        }
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("TAG", "signInWithCredential:failure", task.exception)
                    failed.invoke("Something went wrong")
                }
            }
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun firebaseSignup(
        credential: SignInCredential,
        runner: FoodieUser,
        success: () -> Unit,
        failed: (msg: String) -> Unit
    ) {
        val path = firestore.collection("runnerProfiles").document(credential.id.transformedEmailId())
        val authCredential = GoogleAuthProvider.getCredential(credential.googleIdToken, null)
        auth.signInWithCredential(authCredential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("TAG", "signInWithCredential:success")
                    //val user = auth.currentUser!!
                    path.get()
                        .addOnSuccessListener { document ->
                            if (document != null && document.exists()) {
                                Log.d("TAG", "DocumentSnapshot data: ${document.data}")
                                failed.invoke("Already Registered")
                            } else {
                                val shopStoreRef = storage.reference.child("runner/${runner.email}")
                                GlobalScope.launch(Dispatchers.IO){
                                    var logo = context.copyUriToFile(Uri.parse(runner.pic))
                                    logo = Compressor.compress(context, logo) {
                                        default(height = 360, width = 360, format = Bitmap.CompressFormat.JPEG)
                                    }
                                    val logoLink = shopStoreRef.child("logo.jpg")
                                        .putFile(Uri.fromFile(logo)).await().storage.downloadUrl.await()
                                    Log.d("TAG", "post ${logoLink.path} $logoLink")

                                    path.set(runner.copy(pic = logoLink.toString()))
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
                        }
                        .addOnFailureListener { exception ->
                            Log.d("TAG", "get failed with ", exception)
                            failed.invoke("Something went wrong")

                        }
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("TAG", "signInWithCredential:failure", task.exception)
                    failed.invoke("Something went wrong")
                }
            }

    }

    override fun googleSignIn(activity: Activity, resultLauncher : ActivityResultLauncher<IntentSenderRequest>,
                              failed: (msg: String) -> Unit) {
        Log.e("TAG1", "Google Sign-in")

        val request = GetSignInIntentRequest.builder()
            .setServerClientId(activity.getString(R.string.server_client_id))
            .build()
        Identity.getSignInClient(activity)
            .getSignInIntent(request)
            .addOnSuccessListener { result ->
                try {
                    Log.d("TAG", "googleSignIn: ${result.describeContents()}")
                    Log.e("TAG2", "Google Sign-in")
                    resultLauncher.launch(IntentSenderRequest.Builder(result).build())
                } catch (e: IntentSender.SendIntentException) {
                    Log.e("TAG", "Google Sign-in failed")
                    failed.invoke("Something went wrong")
                }
            }
            .addOnFailureListener { e ->
                Log.e("TAG", "Google Sign-in failed", e)
                failed.invoke("Something went wrong")
            }
    }


}