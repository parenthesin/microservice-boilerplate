(ns microservice-boilerplate.ports.http-out
  (:require [cheshire.core :as json]
            [microservice-boilerplate.adapters :as adapters.price]
            [microservice-boilerplate.schemas.types :as schemas.types]
            [parenthesin.components.http :as components.http]
            [schema.core :as s]))

(s/defn get-btc-usd-price :- s/Num
  [http :- schemas.types/HttpComponent]
  (-> {:url "https://api.coindesk.com/v1/bpi/currentprice.json"
       :method :get}
      (as-> request (components.http/request http request))
      :body
      (json/decode true)
      adapters.price/wire->usd-price))
