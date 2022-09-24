(ns microservice-boilerplate.routes
  (:require [microservice-boilerplate.ports.http-in :as ports.http-in]
            [microservice-boilerplate.schemas.wire-in :as schemas.wire-in]
            [parenthesin.interceptors :as interceptors]
            [schema.core :as s]))

(def routes-definition
  [{:path "/wallet/history"
    :method :get
    :summary "get all wallet entries and current total"
    :responses {200 {:body schemas.wire-in/WalletHistory}
                500 {:body s/Str}}
    :handler ports.http-in/get-history}

   {:path "/wallet/deposit"
    :method :post
    :summary "do a deposit in btc in the wallet"
    :parameters {:body schemas.wire-in/WalletDeposit}
    :responses {201 {:body schemas.wire-in/WalletEntry}
                400 {:body s/Str}
                500 {:body s/Str}}
    :handler ports.http-in/do-deposit!}

   {:path "/wallet/withdrawal"
    :method :post
    :summary "do a withdrawal in btc in the wallet if possible"
    :parameters {:body schemas.wire-in/WalletWithdrawal}
    :responses {201 {:body schemas.wire-in/WalletEntry}
                400 {:body s/Str}
                500 {:body s/Str}}
    :handler ports.http-in/do-withdrawal!}])

(def routes {:routes routes-definition
             :interceptors (merge interceptors/base-interceptors
                                  interceptors/request-logger)})
