(ns microservice-boilerplate.controllers
  (:require [microservice-boilerplate.ports.http-out :as http-out]
            [microservice-boilerplate.schemas.db :as schemas.db]
            [microservice-boilerplate.schemas.types :as schemas.types]
            [schema.core :as s]
            [microservice-boilerplate.logics :as logics]
            [microservice-boilerplate.db :as db]))

(defn- instant-now [] (java.util.Date/from (java.time.Instant/now)))

(s/defn get-wallet :- {:entries :- [schemas.db/WalletEntry]
                       :usd-price :- schemas.types/PositiveNumber}
  [{:keys [http database]} :- schemas.types/Components]
  (let [current-usd-price (http-out/get-btc-usd-price http)
        wallet-entries (db/get-wallet-all-transactions database)]
    {:entries :- wallet-entries
     :usd-price :- current-usd-price}))

(s/defn do-deposit! :- schemas.db/WalletEntry
  [btc :- schemas.types/PositiveNumber
   {:keys [http database]} :- schemas.types/Components]
  (let [now (instant-now)
        current-usd-price (http-out/get-btc-usd-price http)
        entry (logics/->wallet-transaction now btc current-usd-price)]
    (db/insert-wallet-transaction entry database)))

(s/defn do-withdrawal! :- schemas.db/WalletEntry
  [btc :- schemas.types/NegativeNumber
   {:keys [http database]} :- schemas.types/Components]
  (when (logics/can-withdrawal? (db/get-wallet-total database) btc)
    (let [now (instant-now)
          current-usd-price (http-out/get-btc-usd-price http)
          entry (logics/->wallet-transaction now btc current-usd-price)]
      (db/insert-wallet-transaction entry database))))
