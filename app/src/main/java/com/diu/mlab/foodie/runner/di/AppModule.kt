package com.diu.mlab.foodie.runner.di

import android.content.Context
import com.diu.mlab.foodie.runner.data.repo.AuthRepoImpl
import com.diu.mlab.foodie.runner.domain.repo.AuthRepo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    fun provideFirebaseAuth() = Firebase.auth

    @Provides
    fun provideFirebaseUser() = Firebase.auth.currentUser

    @Provides
    @Singleton
    fun provideFirebaseDatabase() = Firebase.database

    @Provides
    @Singleton
    fun provideFirebaseFirestore() = Firebase.firestore

    @Provides
    @Singleton
    fun provideFirebaseStorage() = Firebase.storage

    @Provides
    @Singleton
    fun provideAuthRepo(firebaseAuth: FirebaseAuth, firestore: FirebaseFirestore, storage: FirebaseStorage, @ApplicationContext context: Context): AuthRepo =
        AuthRepoImpl(firebaseAuth, firestore, storage, context)

}