(ns microservice-boilerplate.controllers
  (:require [microservice-boilerplate.ports.http-out :as http-out]
            [microservice-boilerplate.schemas.db :as schemas.db]
            [microservice-boilerplate.schemas.types :as schemas.types]
            [schema.core :as s]
            [microservice-boilerplate.logics :as logics]
            [microservice-boilerplate.db :as db]))

;; TODO
(s/defn get-wallet
  [{:keys [http _database]} :- schemas.types/Components]
  (let [current-usd-price (http-out/get-btc-usd-price http)]
    current-usd-price))

(s/defn do-deposit! :- schemas.db/Wallet
  [btc :- schemas.types/PositiveNumber
   {:keys [http database]} :- schemas.types/Components]
  (let [now (java.time.Instant/now)
        current-usd-price (http-out/get-btc-usd-price http)
        entry (logics/->wallet-entry now btc current-usd-price)]
    (db/insert-wallet-transaction entry database)
    entry))

(s/defn do-withdrawal! :- schemas.db/Wallet
  [btc :- schemas.types/NegativeNumber
   {:keys [http database]} :- schemas.types/Components]
  (let [now (java.time.Instant/now)
        current-usd-price (http-out/get-btc-usd-price http)
        entry (logics/->wallet-entry now btc current-usd-price)]
    (db/insert-wallet-transaction entry database)
    entry))
