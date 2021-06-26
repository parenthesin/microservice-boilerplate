(ns microservice-boilerplate.ports.http-in)

(defn get-history
  [{_components :components}]
  {:status 200
   :body {:entries []
          :total-btc 1M
          :total-current-usd 1M}})

(defn do-deposit!
  [{{_body :body} :parameters
    _components :components}]
  {:status 201
   :body {:id #uuid "d06635f3-a0a8-403c-9448-8f36c7725553"
          :btc-amount 1M
          :usd-amount-at 1M
          :created-at #inst "2021-06-26"}})

(defn do-withdrawal!
  [{{_body :body} :parameters
    _components :components}]
  {:status 201
   :body {:id #uuid "d06635f3-a0a8-403c-9448-8f36c7725553"
          :btc-amount 1M
          :usd-amount-at 1M
          :created-at #inst "2021-06-26"}})
