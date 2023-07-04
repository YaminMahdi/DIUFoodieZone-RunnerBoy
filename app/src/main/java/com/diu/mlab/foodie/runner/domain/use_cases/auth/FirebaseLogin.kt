package com.diu.mlab.foodie.runner.domain.use_cases.auth

import com.diu.mlab.foodie.runner.domain.model.FoodieUser
import com.diu.mlab.foodie.runner.domain.repo.AuthRepo
import com.google.android.gms.auth.api.identity.SignInCredential
import javax.inject.Inject

class FirebaseLogin @Inject constructor (
    val repo: AuthRepo
) {
    operator fun invoke(credential: SignInCredential, success :(runner: FoodieUser) -> Unit, failed :(msg : String) -> Unit)=repo.firebaseLogin(credential, success, failed)
}