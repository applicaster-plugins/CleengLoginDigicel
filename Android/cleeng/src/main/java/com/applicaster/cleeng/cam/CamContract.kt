package com.applicaster.cleeng.cam

import android.util.Log
import com.android.billingclient.api.Purchase
import com.applicaster.authprovider.AuthenticationProviderUtil
import com.applicaster.cam.*
import com.applicaster.cam.params.billing.BillingOffer
import com.applicaster.cam.params.billing.ProductType
import com.applicaster.cleeng.CleengService
import com.applicaster.cleeng.Session
import com.applicaster.cleeng.analytics.AnalyticsDataProvider
import com.applicaster.cleeng.data.Offer
import com.applicaster.cleeng.network.Result
import com.applicaster.cleeng.network.error.WebServiceError
import com.applicaster.cleeng.network.executeRequest
import com.applicaster.cleeng.network.request.*
import com.applicaster.cleeng.network.response.AuthResponseData
import com.applicaster.cleeng.network.response.ResetPasswordResponseData
import com.applicaster.cleeng.network.response.SubscriptionsResponseData
import com.applicaster.cleeng.utils.isNullOrEmpty
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.cancel
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import org.json.JSONObject
import kotlin.coroutines.experimental.CoroutineContext

class CamContract(private val cleengService: CleengService) : ICamContract {
    private val TAG = CamContract::class.java.canonicalName

    //pending offers, androidProductId as key and Cleeng offerID as value
    private val currentOffers: HashMap<String, String> = hashMapOf()

    override fun activateRedeemCode(redeemCode: String, callback: RedeemCodeActivationCallback) {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getPluginConfig() = Session.getPluginConfigurationParams()

    override fun isUserLogged(): Boolean = cleengService.getUserToken().isNotEmpty()

    override fun loadEntitlements(callback: EntitlementsLoadCallback) {
        val requestData = SubscriptionsRequestData(
                1,
                Session.availableProductIds,
                cleengService.getUserToken()
        )
        executeRequest {
            val result = cleengService.networkHelper.requestSubscriptions(requestData)
            when (result) {
                is Result.Success -> {
                    val responseDataResult: List<SubscriptionsResponseData>? = result.value
                    val billingOfferList: ArrayList<BillingOffer> = arrayListOf()
                    currentOffers.clear()
                    responseDataResult?.forEach {
                        val billingOffer = BillingOffer(
                                it.authId.orEmpty(),
                                it.androidProductId.orEmpty(),
                                if (it.period.isNullOrEmpty()) ProductType.INAPP else ProductType.SUBS
                        )
                        billingOfferList.add(billingOffer)
                        Log.i(TAG, "Billing offer: ${billingOfferList[0].productId}")
                        if (!it.androidProductId.isNullOrEmpty() && !it.id.isNullOrEmpty()) {
                            currentOffers[it.androidProductId!!] = it.id!!
                        }
                    }
                    callback.onSuccess(billingOfferList)
                }

                is Result.Failure -> {
                    callback.onFailure(getErrorMessage(result.value))
                }
            }
        }
    }

    override fun login(authFieldsInput: HashMap<String, String>, callback: LoginCallback) {
        executeRequest {
            val result = cleengService.networkHelper.login(
                    authFieldsInput["email"].orEmpty(),
                    authFieldsInput["password"].orEmpty()
            )
            when (result) {
                is Result.Success -> {
                    val responseDataResult: List<AuthResponseData>? = result.value
                    if (responseDataResult != null && responseDataResult.isNotEmpty())
                        cleengService.parseAuthResponse(responseDataResult)
                    callback.onSuccess()
                }

                is Result.Failure -> {
                    callback.onFailure(getErrorMessage(result.value))
                }
            }
        }
    }

    override fun loginWithFacebook(email: String, id: String, callback: FacebookAuthCallback) {
        executeRequest {
            val result = cleengService.networkHelper.loginFacebook(
                    email,
                    id
            )
            when (result) {
                is Result.Success -> {
                    val responseDataResult: List<AuthResponseData>? = result.value
                    if (responseDataResult != null && responseDataResult.isNotEmpty())
                        cleengService.parseAuthResponse(responseDataResult)
                    callback.onSuccess()
                }

                is Result.Failure -> {
                    callback.onFailure(getErrorMessage(result.value))
                }
            }
        }
    }

    override fun signUp(authFieldsInput: HashMap<String, String>, callback: SignUpCallback) {
        executeRequest {
            val result = cleengService.networkHelper.register(
                    RegisterRequestData(
                            authFieldsInput["email"].orEmpty(),
                            authFieldsInput["password"].orEmpty(),
                            null,
                            Session.user?.country.orEmpty(),
                            Session.user?.locale.orEmpty(),
                            Session.user?.currency.orEmpty()
                    )
            )
            when (result) {
                is Result.Success -> {
                    val responseDataResult: List<AuthResponseData>? = result.value
                    if (responseDataResult != null && responseDataResult.isNotEmpty())
                        cleengService.parseAuthResponse(responseDataResult)
                    callback.onSuccess()
                }

                is Result.Failure -> {
                    callback.onFailure(getErrorMessage(result.value))
                }
            }
        }
    }

    override fun signupWithFacebook(email: String, id: String, callback: FacebookAuthCallback) {
        executeRequest {
            val result = cleengService.networkHelper.registerFacebook(
                    RegisterRequestData(
                            email,
                            null,
                            id,
                            Session.user?.country.orEmpty(),
                            Session.user?.locale.orEmpty(),
                            Session.user?.currency.orEmpty()
                    )
            )
            when (result) {
                is Result.Success -> {
                    val responseDataResult: List<AuthResponseData>? = result.value
                    if (responseDataResult != null && responseDataResult.isNotEmpty())
                        cleengService.parseAuthResponse(responseDataResult)
                    callback.onSuccess()
                }

                is Result.Failure -> {
                    callback.onFailure(getErrorMessage(result.value))
                }
            }
        }
    }

    override fun onItemPurchased(purchase: List<Purchase>, callback: PurchaseCallback) {
        purchase.forEach { subscribeOn(it, callback) }
    }

    override fun onPurchasesRestored(purchases: List<Purchase>, callback: RestoreCallback) {
        //Test fun for new restore API.
        // TODO: Uncomment when server-side implementation will be finished
//        sendRestoredSubscriptions(purchases, callback)
        //Restore implementation based on regular purchase server API (i.e. /subscription)
        //TODO: Remove this when server-side implementation will be finished
        purchases.forEach { subscribeOn(it, callback) }
    }

    /**
     *  Test fun for new restore API. Use it in onPurchasesRestored callback
     */
    private fun sendRestoredSubscriptions(
            purchases: List<Purchase>,
            callback: RestoreCallback
    ) {
        val receipts = arrayListOf<PaymentReceipt>()
        purchases.forEach { purchaseItem ->
            val purchaseState = JSONObject(purchaseItem.originalJson).getDouble("purchaseState").toInt()
            receipts.add(
                    PaymentReceipt(
                            "",
                            purchaseItem.orderId,
                            purchaseItem.packageName,
                            purchaseItem.sku,
                            purchaseState,
                            purchaseItem.purchaseTime.toString(),
                            purchaseItem.purchaseToken
                    )
            )
        }
        val restoreSubsData = RestoreSubscriptionsRequestData(
                receipts,
                cleengService.getUserToken()
        )
        executeRequest {
            val result = cleengService.networkHelper.restoreSubscriptions(restoreSubsData)
            when (result) {
                is Result.Success -> {
                    finishPurchaseFlow("", callback)
                }

                is Result.Failure -> {
                    callback.onFailure("")
                    Log.e(TAG, result.value?.name)
                }
            }
        }
    }

    private fun subscribeOn(purchaseItem: Purchase, callback: ActionCallback) {
        val offerEntry = currentOffers.entries.find {
            purchaseItem.sku == it.key
        }

        val purchaseState = JSONObject(purchaseItem.originalJson).getDouble("purchaseState").toInt()

        val receipt = PaymentReceipt(
                "",
                purchaseItem.orderId,
                purchaseItem.packageName,
                purchaseItem.sku,
                purchaseState,
                purchaseItem.purchaseTime.toString(),
                purchaseItem.purchaseToken
        )

        val subscribeRequestData = SubscribeRequestData(
                offerEntry?.value,
                receipt,
                cleengService.getUserToken()
        )

        executeRequest {
            val result = cleengService.networkHelper.subscribe(subscribeRequestData)
            when (result) {
                is Result.Success -> {
                    finishPurchaseFlow(offerEntry!!.value, callback)
                }

                is Result.Failure -> {
                    callback.onFailure("")
                    Log.e(TAG, result.value?.name)
                }
            }
        }
    }

    /**
     * Registering purchases on the Cleeng server and waiting until it will return response with updated
     * offerIDs, authIDs and purchase tokens
     */
    private fun finishPurchaseFlow(purchasedOfferId: String, callback: ActionCallback) {
        var registeredOffers: List<AuthResponseData> = arrayListOf()
        val coroutineContext: CoroutineContext = UI
        launch(coroutineContext) {
            try {
                repeat(PURCHASE_VERIFICATION_CALL_MAX_NUM) {
                    val result = cleengService.networkHelper.extendToken(cleengService.getUserToken())
                    when (result) {
                        is Result.Success -> {
                            result.value?.forEach {
                                if (it.offerId.isNullOrEmpty()) {
                                    cleengService.saveUserToken(it.token!!)
                                } else if (it.offerId == purchasedOfferId) {
                                    registeredOffers = result.value
                                    coroutineContext.cancel()
                                    return@repeat
                                }
                            }
                        }
                    }
                    delay(PURCHASE_VERIFICATION_DELAY_MILLIS)
                }
            } finally {
                saveOwnedUserProducts(registeredOffers, callback)
            }
        }
    }

    private fun saveOwnedUserProducts(registeredOffers: List<AuthResponseData>, callback: ActionCallback) {
        if (registeredOffers.isNotEmpty()) {
            Log.d(TAG, "saveOwnedUserProducts with ${registeredOffers}")
            val offers = arrayListOf<Offer>()
            val ownedProductIds = hashSetOf<String>()
            registeredOffers.forEach { authData ->
                offers.add(Offer(authData.offerId, authData.token, authData.authId))
                ownedProductIds.add(authData.authId.orEmpty())
                //saving token in the applicaster SDK. Later this token will be used by the player
                AuthenticationProviderUtil.addToken(authData.authId, authData.token)
            }

            Session.setUserOffers(offers)
            Session.addOwnedProducts(ownedProductIds)
            callback.onSuccess()
        } else {
            Log.d(TAG, "saveOwnedUserProducts with ${registeredOffers}")
            callback.onFailure(Session.pluginConfigurator?.getCleengErrorMessage(WebServiceError.DEFAULT).orEmpty())
        }
    }

    override fun isPurchaseRequired(): Boolean = !Session.isAccessGranted()

    override fun resetPassword(authFieldsInput: HashMap<String, String>, callback: PasswordResetCallback) {
        executeRequest {
            val result = cleengService.networkHelper.resetPassword(
                    authFieldsInput["email"].orEmpty()
            )
            when (result) {
                is Result.Success -> {
                    val responseDataResult: ResetPasswordResponseData? = result.value
                    if (responseDataResult?.success == true)
                        callback.onSuccess()
                    else
                        callback.onFailure("Error")

                }

                is Result.Failure -> {
                    callback.onFailure(getErrorMessage(result.value))
                }
            }
        }
    }

    override fun isRedeemActivated(): Boolean = false //TODO: dummy. add proper handling

    private fun getErrorMessage(webError: WebServiceError?): String {
        return Session.pluginConfigurator?.getCleengErrorMessage(webError
                ?: WebServiceError.DEFAULT).orEmpty()
    }

    override fun getCamFlow(): CamFlow = Session.getCamFlow()

    override fun onCamFinished() {
        cleengService.startUpHookListener?.onHookFinished()
    }

    override fun getAnalyticsDataProvider(): IAnalyticsDataProvider {
        return AnalyticsDataProvider()
    }

    companion object {
        const val PURCHASE_VERIFICATION_CALL_MAX_NUM = 12
        const val PURCHASE_VERIFICATION_DELAY_MILLIS = 5000L
    }
}
