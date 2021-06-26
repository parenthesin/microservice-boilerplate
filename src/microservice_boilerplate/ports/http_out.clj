(ns microservice-boilerplate.ports.http-out
  (:require [parenthesin.components.http :as components.http]
            [microservice-boilerplate.adapters :as adapters.price]
            [microservice-boilerplate.schemas.types :as schemas.types]
            [schema.core :as s]))

(s/defn get-btc-usd-price :- s/Num
  [{:keys [http]} :- schemas.types/Components]
  (->> {:url "https://api.coindesk.com/v1/bpi/currentprice.json" :method :get}
       (components.http/request http)
       adapters.price/wire->usd-price))
