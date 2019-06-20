//
//  CleengAPI.swift
//  CleengLogin
//
//  Created by Egor Brel on 6/6/19.
//

import Foundation
import CAM
import Alamofire

enum CleengAPI {
    case login(publisherID: String, email: String?, password: String?)
    case loginWithFacebook(publisherID: String, email: String?, facebookId: String?)
    case register(publisherID: String, email: String?, password: String?)
    case registerWithFacebook(publisherID: String, email: String?, facebookId: String?)
    case resetPassword(publisherID: String, email: String?)
    case extendToken(publisherID: String, token: String?)
    case subscriptions(publisherID: String, token: String?, byAuthId: Int, offers: [String]?)
    case subscription(publisherID: String, transactionId: String?, receiptData: String?, token: String?, offerId: String?, isRestored: Bool, couponCode: String?)
}

extension CleengAPI {
    var baseLink: String {
        return "https://applicaster-cleeng-sso.herokuapp.com/"
    }
    
    var path: String {
        switch self {
        case .login, .loginWithFacebook:
            return "login"
        case .register, .registerWithFacebook:
            return "register"
        case .resetPassword:
            return "passwordReset"
        case .extendToken:
            return "extendToken"
        case .subscriptions:
            return "subscriptions"
        case .subscription:
            return "subscription"
        }
    }
    
    var url: URL {
        return URL(string: baseLink + path)!
    }
    
    var httpMethod: HTTPMethod {
        return .post
    }
    
    var params: [String: Any] {
        switch self {
        case .login(let publisherID, let email, let password):
            return ["publisherId": publisherID, "email": email ?? "", "password": password ?? ""]
        case .loginWithFacebook(let publisherID, let email, let facebookId):
            return ["publisherId": publisherID, "email": email ?? "", "facebookId": facebookId ?? ""]
        case .register(let publisherID, let email, let password):
            let locale = Locale.current
            var country = ""
            if let countryCode = (locale as NSLocale).object(forKey: .countryCode) as? String {
                country = countryCode
            }
            return ["publisherId": publisherID, "email": email ?? "", "password": password ?? "",
                    "country": country, "locale": "en_US", "currency": "USD"]
        case .registerWithFacebook(let publisherID, let email, let facebookId):
            let locale = Locale.current
            var country = ""
            if let countryCode = (locale as NSLocale).object(forKey: .countryCode) as? String {
                country = countryCode
            }
            return ["publisherId": publisherID, "email": email ?? "", "facebookId": facebookId ?? "",
                    "country": country, "locale": "en_US", "currency": "USD"]
        case .resetPassword(let publisherID, let email):
            return ["publisherId": publisherID, "email": email ?? ""]
        case .extendToken(let publisherID, let token):
            return ["publisherId": publisherID, "token": token ?? ""]
        case .subscriptions(let publisherID, let token, let byAuthId, let offers):
            return ["publisherId": publisherID, "token": token ?? "", "byAuthId": byAuthId,
                    "offers": offers ?? [String]()]
        case .subscription(let publisherID, let transactionId, let receiptData, let token, let offerId, let isRestored, let couponCode):
            let receiptInfo: [String: Any] = [
                "transactionId": transactionId ?? "",
                "receiptData": receiptData ?? ""
            ]
            let params: [String: Any] = [
                "publisherID": publisherID,
                "appType": "ios",
                "receipt": receiptInfo,
                "offerId": offerId ?? "",
                "token": token ?? "",
                "isRestored": isRestored,
                "couponCode": couponCode ?? ""
            ]
            return params
        }
    }
}
