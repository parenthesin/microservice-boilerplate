(ns microservice-boilerplate.ports.http-out
  (:require [microservice-boilerplate.adapters :as adapters.price]
            [microservice-boilerplate.schemas.types :as schemas.types]
            [parenthesin.components.http.clj-http :as components.http]
            [schema.core :as s]))

(s/defn get-btc-usd-price :- s/Num
  [http :- schemas.types/HttpComponent]
  (->> {:url "https://api.kraken.com/0/public/Ticker?pair=XBTUSD"
        :as :json
        :method :get}
       (components.http/request http)
       :body
       adapters.price/wire->usd-price))
