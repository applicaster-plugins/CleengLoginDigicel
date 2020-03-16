package com.applicaster.cleeng.network

import com.applicaster.cleeng.network.request.RestoreSubscriptionsRequestData
import com.applicaster.cleeng.network.request.SubscribeRequestData
import com.applicaster.cleeng.network.request.SubscriptionsRequestData
import com.applicaster.cleeng.network.response.AuthResponseData
import com.applicaster.cleeng.network.response.ResetPasswordResponseData
import com.applicaster.cleeng.network.response.RestoreSubscriptionsResponseData
import com.applicaster.cleeng.network.response.SubscriptionsResponseData
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface RestService {

    @FormUrlEncoded
    @POST("register")
    suspend fun registerEmail(
        @Field("email") email: String,
        @Field("password") password: String,
        @Field("country") country: String,
        @Field("locale") locale: String,
        @Field("currency") currency: String
    ): Response<List<AuthResponseData>>

    @FormUrlEncoded
    @POST("register")
    suspend fun registerFacebook(
        @Field("email") email: String,
        @Field("facebookId") facebookId: String,
        @Field("country") country: String,
        @Field("locale") locale: String,
        @Field("currency") currency: String
    ): Response<List<AuthResponseData>>

    @FormUrlEncoded
    @POST("login")
    suspend fun loginEmail(
        @Field("email") email: String,
        @Field("password") password: String
    ): Response<List<AuthResponseData>>

    @FormUrlEncoded
    @POST("login")
    suspend fun loginFacebook(
        @Field("email") email: String,
        @Field("facebookId") facebookId: String
    ): Response<List<AuthResponseData>>

    @FormUrlEncoded
    @POST("passwordReset")
    suspend fun resetPassword(
        @Field("email") email: String
    ): Response<ResetPasswordResponseData>

    @FormUrlEncoded
    @POST("extendToken")
    suspend fun extendToken(
        @Field("token") token: String
    ): Response<List<AuthResponseData>>

    @POST("subscriptions")
    suspend fun requestSubscriptions(
        @Body subscriptionsRequestData: SubscriptionsRequestData
    ): Response<List<SubscriptionsResponseData>>

    @POST("subscription")
    suspend fun subscribe(
        @Body subscribeRequestData: SubscribeRequestData
    ): Response<Unit>

    @POST("restoreSubscriptions")
    suspend fun restoreSubscriptions(
        @Body restoreSubscriptionsData: RestoreSubscriptionsRequestData
    ): Response<List<RestoreSubscriptionsResponseData>>

    @FormUrlEncoded
    @POST("generateCustomerToken")
    suspend fun generateCustomerToken(
            @Field("email") email: String
    ): Response<List<AuthResponseData>>
}