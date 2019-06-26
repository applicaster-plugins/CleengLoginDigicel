package com.applicaster.cleeng.data

import com.applicaster.cam.params.billing.Offer

data class User(
    val email: String? = null,
    val password: String? = null,
    val facebookId: String? = null,
    var token: String? = null,
    var userOffers: ArrayList<Offer>? = null,
    var ownedSubscriptions: ArrayList<Subscription>? = null,
    val country: String = "US",
    val locale: String = "en_US",
    val currency: String = "USD"
)