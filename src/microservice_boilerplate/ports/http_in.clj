(ns microservice-boilerplate.ports.http-in
  (:require [microservice-boilerplate.controllers :as controllers]))

(defn get-history
  [{components :components}]
  {:status 200
   :body (controllers/get-wallet components)})

(defn do-deposit!
  [{{{:keys [btc]} :body} :parameters
    components :components}]
  (if (pos? btc)
    {:status 201
     :body (controllers/do-deposit! btc components)}
    {:status 400
     :body "btc deposit amount can't be negative."}))

(defn do-withdrawal!
  [{{{:keys [btc]} :body} :parameters
    components :components}]
  (if (neg? btc)
    (if-let [withdrawal (controllers/do-withdrawal! btc components)]
      {:status 201
       :body withdrawal}
      {:status 400
       :body "withdrawal amount bigger than the total in the wallet."})
    {:status 400
     :body "btc withdrawal amount can't be positive."}))
