(ns microservice-boilerplate.adapters
  (:require [microservice-boilerplate.schemas.wire-out :as schemas.wire-out]
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
