package com.diu.mlab.foodie.runner.data.repo

import android.app.Activity
import android.content.Context
import android.credentials.GetCredentialException
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.diu.mlab.foodie.runner.domain.RequestState
import com.diu.mlab.foodie.runner.domain.model.FoodieUser
import com.diu.mlab.foodie.runner.domain.repo.AuthRepo
import com.diu.mlab.foodie.runner.util.Constants
import com.diu.mlab.foodie.runner.util.copyUriToFile
import com.diu.mlab.foodie.runner.util.transformedEmailId
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
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
import kotlinx.coroutines.withContext
import javax.inject.Inject


class AuthRepoImpl @Inject constructor(
    private val auth : FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val context: Context
    ) : AuthRepo {


    override fun firebaseLogin(
        credential: GoogleIdTokenCredential,
        success: (runner: FoodieUser) -> Unit,
        failed: (msg: String) -> Unit
    ) {
        val authCredential = GoogleAuthProvider.getCredential(credential.idToken, null)
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
        credential: GoogleIdTokenCredential,
        runner: FoodieUser,
        success: () -> Unit,
        failed: (msg: String) -> Unit
    ) {
        val path = firestore.collection("runnerProfiles").document(credential.id.transformedEmailId())
        val authCredential = GoogleAuthProvider.getCredential(credential.idToken, null)
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

//     fun googleSignIn(activity: Activity, resultLauncher : ActivityResultLauncher<IntentSenderRequest>,
//                              failed: (msg: String) -> Unit) {
//        Log.e("TAG1", "Google Sign-in")
//
//        val request = GetSignInIntentRequest.builder()
//            .setServerClientId(activity.getString(R.string.server_client_id))
//            .build()
//        Identity.getSignInClient(activity)
//            .getSignInIntent(request)
//            .addOnSuccessListener { result ->
//                try {
//                    Log.d("TAG", "googleSignIn: ${result.describeContents()}")
//                    Log.e("TAG2", "Google Sign-in")
//                    resultLauncher.launch(IntentSenderRequest.Builder(result).build())
//                } catch (e: IntentSender.SendIntentException) {
//                    Log.e("TAG", "Google Sign-in failed")
//                    failed.invoke("Something went wrong")
//                }
//            }
//            .addOnFailureListener { e ->
//                Log.e("TAG", "Google Sign-in failed", e)
//                failed.invoke("Something went wrong")
//            }
//    }

    override suspend fun googleSignIn(activity: Activity, isAuthorized : Boolean): RequestState<GoogleIdTokenCredential>{
        return withContext(Dispatchers.IO){
//            val signInGoogleOption = GetSignInWithGoogleOption
//                .Builder(Constants.SERVER_CLIENT_ID)
//                .build()
            val signInGoogleOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(isAuthorized)
                .setServerClientId(Constants.SERVER_CLIENT_ID)
                .build()
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(signInGoogleOption)
                .build()
            try {
                val result = CredentialManager
                    .create(context)
                    .getCredential(activity, request)
                val credential = result.credential
                if(credential is CustomCredential &&
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL){
                    val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    RequestState.Success(googleCredential)
                }else
                    RequestState.Error("Only Google Account Allowed")
            } catch (e: GetCredentialCancellationException) {
                e.printStackTrace()
                if (e.type == GetCredentialException.TYPE_USER_CANCELED)
                    RequestState.Error("Canceled by user.",20)
                else
                    RequestState.Error(e.message ?: "Google Sign In Error!")
            } catch (e: Exception) {
                e.printStackTrace()
                RequestState.Error(e.localizedMessage ?: "Google Sign In Error!")
            }
        }
    }


}