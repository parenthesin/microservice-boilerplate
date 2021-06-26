(ns microservice-boilerplate.controllers
  (:require [microservice-boilerplate.ports.http-out :as http-out]
            [microservice-boilerplate.schemas.types :as schemas.types]
            [microservice-boilerplate.schemas.wire-in :as schemas.wire-in]
            [schema.core :as s]))

(s/defn get-wallet
  [{:keys [http _database]} :- schemas.types/Components]
  (let [current-usd-price (http-out/get-btc-usd-price http)]
    current-usd-price))

(s/defn do-deposit!
  [_body :- schemas.wire-in/WalletHistory
   {:keys [http _database]} :- schemas.types/Components]
  (let [current-usd-price (http-out/get-btc-usd-price http)]
    current-usd-price))

(s/defn do-withdrawal!
  [_body
   {:keys [http _database]} :- schemas.types/Components]
  (let [current-usd-price (http-out/get-btc-usd-price http)]
    current-usd-price))
