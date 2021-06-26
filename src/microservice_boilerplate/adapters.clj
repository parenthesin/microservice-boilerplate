(ns microservice-boilerplate.adapters
  (:require [microservice-boilerplate.schemas.wire-out :as schemas.wire-out]
            [schema.core :as s]))

(s/defn wire->usd-price  :- s/Num
  [wire :- schemas.wire-out/CoinDeskResponse]
  (-> wire
      (get-in [:bpi :USD :rate_float])
      bigdec))
