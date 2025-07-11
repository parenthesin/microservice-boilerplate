(ns microservice-boilerplate.adapters
  (:require [microservice-boilerplate.schemas.db :as schemas.db]
            [microservice-boilerplate.schemas.types :as schemas.types]
            [microservice-boilerplate.schemas.wire-in :as schemas.wire-in]
            [microservice-boilerplate.schemas.wire-out :as schemas.wire-out]
            [schema.core :as s])
  (:import [java.time LocalDateTime ZoneId]
           [java.time.format DateTimeFormatter]))

(s/defn ^:private date->localdatetime :- LocalDateTime
  [value :- s/Inst
   zone-id :- ZoneId]
  (-> value
      (.toInstant)
      (.atZone zone-id)
      (.toLocalDateTime)))

(s/defn inst->utc-formated-string :- s/Str
  [inst :- s/Inst
   str-format :- s/Str]
  (-> inst
      (date->localdatetime (ZoneId/of "UTC"))
      (.format (DateTimeFormatter/ofPattern str-format))))

(s/defn wire->usd-price  :- s/Num
  [wire :- schemas.wire-out/KrakenResponse]
  (-> wire
      (get-in [:result :XXBTZUSD :c])
      first
      bigdec))

(s/defn ^:private wire-in->db  :- schemas.db/WalletTransaction
  [id :- s/Uuid
   btc :- s/Num
   usd :- schemas.types/PositiveNumber]
  {:wallet/id id
   :wallet/btc_amount btc
   :wallet/usd_amount_at usd})

(s/defn deposit->db  :- schemas.db/WalletTransaction
  [id :- s/Uuid
   btc :- schemas.types/PositiveNumber
   usd :- schemas.types/PositiveNumber]
  (wire-in->db id btc usd))

(s/defn withdrawal->db  :- schemas.db/WalletTransaction
  [id :- s/Uuid
   btc :- schemas.types/NegativeNumber
   usd :- schemas.types/PositiveNumber]
  (wire-in->db id btc usd))

(s/defn db->wire-in :- schemas.wire-in/WalletEntry
  [{:wallet/keys [id btc_amount usd_amount_at created_at]} :- schemas.db/WalletEntry]
  {:id id
   :btc-amount (bigdec btc_amount)
   :usd-amount-at (bigdec usd_amount_at)
   :created-at created_at})

(s/defn ->wallet-history :- schemas.wire-in/WalletHistory
  [current-usd-price :- schemas.types/PositiveNumber
   wallet-entries :- [schemas.db/WalletEntry]]
  (let [total-btc (reduce #(+ (:wallet/btc_amount %2) %1) 0M wallet-entries)]
    {:entries (mapv db->wire-in wallet-entries)
     :total-btc (bigdec total-btc)
     :total-current-usd (bigdec (* current-usd-price total-btc))}))
