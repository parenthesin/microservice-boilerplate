(ns microservice-boilerplate.logics
  (:require [microservice-boilerplate.adapters :as adapters]
            [microservice-boilerplate.schemas.db :as schemas.db]
            [microservice-boilerplate.schemas.types :as schemas.types]
            [schema.core :as s])
  (:import [java.util UUID]))

(s/defn uuid-from-string :- s/Uuid
  [seed :- s/Str]
  (-> seed
      .getBytes
      UUID/nameUUIDFromBytes))

(s/defn uuid-from-date-amount :- s/Uuid
  [date :- s/Inst
   amount :- s/Num]
  (-> date
      (adapters/inst->utc-formated-string "yyyy-MM-dd hh:mm:ss")
      (str amount)
      uuid-from-string))

(s/defn ->wallet-transaction :- schemas.db/WalletTransaction
  [date :- s/Inst
   amount :- s/Num
   current-usd-price :- schemas.types/PositiveNumber]
  #:wallet{:id (uuid-from-date-amount date amount)
           :btc_amount amount
           :usd_amount_at (* current-usd-price amount)})

(s/defn can-withdrawal? :- s/Bool
  [withdrawal-amount :- schemas.types/NegativeNumber
   current-total :- schemas.types/PositiveNumber]
  (-> (+ current-total withdrawal-amount)
      (>= 0)))
