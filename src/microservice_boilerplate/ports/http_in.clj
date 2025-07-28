(ns microservice-boilerplate.ports.http-in
  (:require [microservice-boilerplate.adapters :as adapters]
            [microservice-boilerplate.controllers :as controllers]))

(defn get-btc-usd-price
  [{components :components}]
  {:status 200
   :body {:btc-amount 1
          :usd-amount (controllers/get-btc-usd-price components)}})

(defn get-history
  [{components :components}]
  (let [{:keys [entries usd-price]} (controllers/get-wallet components)]
    {:status 200
     :body (adapters/->wallet-history usd-price entries)}))

(defn do-deposit!
  [{{{:keys [btc]} :body} :parameters
    components :components}]
  (if (pos? btc)
    {:status 201
     :body (-> btc
               (controllers/do-deposit! components)
               adapters/db->wire-in)}
    {:status 400
     :body "btc deposit amount can't be negative."}))

(defn do-withdrawal!
  [{{{:keys [btc]} :body} :parameters
    components :components}]
  (if (neg? btc)
    (if-let [withdrawal (controllers/do-withdrawal! btc components)]
      {:status 201
       :body (adapters/db->wire-in withdrawal)}
      {:status 400
       :body "withdrawal amount bigger than the total in the wallet."})
    {:status 400
     :body "btc withdrawal amount can't be positive."}))
