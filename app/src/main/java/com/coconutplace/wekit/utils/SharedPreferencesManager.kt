package com.coconutplace.wekit.utils

import android.content.Context
import android.content.SharedPreferences
import com.coconutplace.wekit.data.entities.BodyInfo
import com.coconutplace.wekit.data.entities.User
import com.google.gson.Gson

class SharedPreferencesManager(private val context: Context){
    companion object{
        const val TAG = "MOBILE_TEMPLATE_APP"
        const val X_ACCESS_TOKEN = "X-ACCESS-TOKEN"
        const val ERROR_TAG = "WEKIT_ERROR_TAG"
        const val CHECK_TAG = "WEKIT_CHECK_TAG"
        const val CLIENT_ID = "CLIENT_ID"
        const val NICKNAME = "NICKNAME"
        const val PUSH_FLAG = "PUSH_FLAG"
        const val BODY = "BODY"
        const val USER = "USER"
        const val EMAIL = "EMAIL"
    }

    fun getSharedPreferences() : SharedPreferences {
        return context.getSharedPreferences(TAG, Context.MODE_PRIVATE)
    }

    fun removeAll(){
        val spf = getSharedPreferences()
        val editor = spf.edit()
        editor.remove(X_ACCESS_TOKEN)
        editor.remove(CLIENT_ID)
        editor.remove(NICKNAME)
        editor.remove(PUSH_FLAG)
        editor.remove(BODY)
        editor.apply()
    }

    fun saveUser(user: User){
        val spf = getSharedPreferences()
        val editor = spf.edit()
        val gson = Gson()
        val json = gson.toJson(user)
        editor.putString(USER, json)
        editor.apply()
    }

    fun getUser() : User? {
        val gson = Gson()
        val json = getSharedPreferences().getString(USER, null)

        return gson.fromJson(json, User::class.java)
    }

    fun removeUser(){
        val spf = getSharedPreferences()
        val editor = spf.edit()
        editor.remove(USER)
        editor.apply()
    }

    fun getJwtToken() : String? {
        return getSharedPreferences().getString(X_ACCESS_TOKEN, null)
    }

    fun saveJwtToken(jwtToken: String){
        val spf = getSharedPreferences()
        val editor = spf.edit()
        editor.putString(X_ACCESS_TOKEN, jwtToken)
        editor.apply()
    }

    fun removeJwtToken(){
        val spf = getSharedPreferences()
        val editor = spf.edit()
        editor.remove(X_ACCESS_TOKEN)
        editor.apply()
    }

    fun saveClientID(id: String){
        val spf = getSharedPreferences()
        val editor = spf.edit()
        editor.putString(CLIENT_ID, id)
        editor.apply()
    }

    fun getClientID() : String? {
        return getSharedPreferences().getString(CLIENT_ID, null)
    }

    fun removeClientID(){
        val spf = getSharedPreferences()
        val editor = spf.edit()
        editor.remove(CLIENT_ID)
        editor.apply()
    }

    fun saveNickname(nickname: String){
        val spf = getSharedPreferences()
        val editor =  spf.edit()
        editor.putString(NICKNAME, nickname)
        editor.apply()
    }

    fun getNickname() : String{
        return getSharedPreferences().getString(NICKNAME, "").toString()
    }

    fun removeNickname(){
        val spf = getSharedPreferences()
        val editor = spf.edit()
        editor.remove(NICKNAME)
        editor.apply()
    }


    fun saveEmail(email: String){
        val spf = getSharedPreferences()
        val editor =  spf.edit()
        editor.putString(EMAIL, email)
        editor.apply()
    }

    fun getEmail() : String?{
        return getSharedPreferences().getString(EMAIL, null)
    }

    fun removeEmail(){
        val spf = getSharedPreferences()
        val editor = spf.edit()
        editor.remove(EMAIL)
        editor.apply()
    }

    fun savePushNotificationFlag(on: Boolean){
        val spf = getSharedPreferences()
        val editor = spf.edit()
        editor.putBoolean(PUSH_FLAG,on)
        editor.apply()
    }

    fun getPushNotificationFLag():Boolean{
        return getSharedPreferences().getBoolean(PUSH_FLAG,true)
    }

    fun removePushNotificationFlag() {
        val spf = getSharedPreferences()
        val editor = spf.edit()
        editor.remove(PUSH_FLAG)
        editor.apply()
    }

    fun saveBody(body: BodyInfo){
        val spf = getSharedPreferences()
        val editor = spf.edit()
        val gson = Gson()
        val bodyJson: String = gson.toJson(body)
        editor.putString(BODY, bodyJson)
        editor.apply()
    }

    fun getBody() : String?{
        return getSharedPreferences().getString(BODY, null)
    }

    fun removeBody(){
        val spf = getSharedPreferences()
        val editor = spf.edit()
        editor.remove(BODY)
        editor.apply()
    }
}