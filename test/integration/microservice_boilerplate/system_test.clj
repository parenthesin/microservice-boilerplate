(ns integration.microservice-boilerplate.system-test
  (:require [clojure.test :as clojure.test]
            [integration.microservice-boilerplate.aux :as aux]
            [integration.microservice-boilerplate.aux.database :as aux.database]
            [integration.microservice-boilerplate.aux.http :as aux.http]
            [integration.microservice-boilerplate.aux.webserver :as aux.webserver]
            [microservice-boilerplate.components.database :as components.database]
            [microservice-boilerplate.components.http :as components.http]
            [schema.core :as s]
            [schema.test :as schema.test]
            [state-flow.api :refer [defflow]]
            [state-flow.assertions.matcher-combinators :refer [match?]]
            [state-flow.core :as state-flow :refer [flow]]))

(clojure.test/use-fixtures :once schema.test/validate-schemas)

(defn do-deposit!
  [{{{:keys [btc]} :body} :parameters
    {:keys [http database]} :components}]
  (let [response (components.http/request http {:url "http://coinbase.org" :method :get})
        rate (get-in response [:body :rate])
        price (* rate btc)]
    (components.database/execute database [(str "insert into wallet(price) values('" price "')")])
    {:status 201
     :body {:usd price}}))

(defn get-wallet
  [{{:keys [database]} :components}]
  (let [wallet (components.database/execute database ["select * from wallet"])]
    {:status 200
     :body (map (fn [{:wallet/keys [id price]}]
                  {:id id
                   :amount price})
                wallet)}))

(def test-routes
  [["/wallet"
    {:swagger {:tags ["wallet"]}}

    ["/deposit"
     {:post {:summary "deposit btc and return value in usd"
             :parameters {:body {:btc s/Num}}
             :responses {201 {:body {:usd s/Num}}}
             :handler do-deposit!}}]

    ["/list"
     {:get {:summary "list deposits in wallet"
            :responses {200 {:body [{:id s/Int :amount BigDecimal}]}}
            :handler get-wallet}}]]])

(defflow
  flow-integration-system-test
  {:init (partial aux/start-system! test-routes)
   :cleanup aux/stop-system!
   :fail-fast? true}
  (flow "should interact with system"

    (flow "prepare system with http-out mocks and creating tables"
      (aux.http/set-http-out-responses! {"http://coinbase.org" {:body {:rate 35000.0M}
                                                                :status 200}})

      (aux.database/execute! ["create table if not exists wallet (
                                  id serial primary key,
                                  price decimal)"])

      (flow "should insert deposit into wallet"
        (match? {:status 201
                 :body {:usd 70000.0}}
                (aux.webserver/request! {:method :post
                                         :uri    "/wallet/deposit"
                                         :body   {:btc 2M}})))

      (flow "should list wallet deposits"
        (match? {:status 200
                 :body [{:id 1
                         :amount 70000.0}]}
                (aux.webserver/request! {:method :get
                                         :uri    "/wallet/list"}))))))
