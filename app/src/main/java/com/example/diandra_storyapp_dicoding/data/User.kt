package com.example.diandra_storyapp_dicoding.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val token: String,
    val isLogin: Boolean
) : Parcelable
