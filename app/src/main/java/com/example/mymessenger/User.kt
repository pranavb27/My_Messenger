package com.example.mymessenger

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class User(val username: String, val uid: String, val profileImageUrl: String ) : Parcelable
{
    //implementing the empty constructor for getting user details from databse while displaying in new message activity
    constructor() : this("","","") //if you dont do this app will crash
}