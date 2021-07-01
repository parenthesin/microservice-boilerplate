(ns microservice-boilerplate.ports.http-out
  (:require [parenthesin.components.http :as components.http]
            [microservice-boilerplate.adapters :as adapters.price]
            [microservice-boilerplate.schemas.types :as schemas.types]
            [schema.core :as s]))

(s/defn get-btc-usd-price :- s/Num
  [http :- schemas.types/HttpComponent]
  (->> {:url "https://api.coindesk.com/v1/bpi/currentprice.json"
        :as :json
        :method :get}
       (components.http/request http)
       :body
       adapters.price/wire->usd-price))
