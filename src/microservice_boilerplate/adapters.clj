(ns microservice-boilerplate.adapters
  (:require [microservice-boilerplate.schemas.wire-out :as schemas.wire-out]
            [microservice-boilerplate.schemas.db :as schemas.db]
            [microservice-boilerplate.schemas.types :as schema.types]
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
  [wire :- schemas.wire-out/CoinDeskResponse]
  (-> wire
      (get-in [:bpi :USD :rate_float])
      bigdec))

(s/defn ^:private wire-in->db  :- schemas.db/Wallet
  [id :- s/Uuid
   btc :- s/Num
   usd :- schema.types/PositiveNumber]
  {:wallet/id id
   :wallet/btc_amount btc
   :wallet/usd_amount_at usd})

(s/defn deposit->db  :- schemas.db/Wallet
  [id :- s/Uuid
   btc :- schema.types/PositiveNumber
   usd :- schema.types/PositiveNumber]
  (wire-in->db id btc usd))

(s/defn withdrawal->db  :- schemas.db/Wallet
  [id :- s/Uuid
   btc :- schema.types/NegativeNumber
   usd :- schema.types/PositiveNumber]
  (wire-in->db id btc usd))