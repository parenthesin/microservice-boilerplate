(ns integration.parenthesin.system-test
  (:require [cheshire.core :as json]
            [clojure.test :as clojure.test]
            [integration.parenthesin.util :as aux]
            [integration.parenthesin.util.database :as util.database]
            [integration.parenthesin.util.http :as util.http]
            [integration.parenthesin.util.webserver :as util.webserver]
            [parenthesin.components.database :as components.database]
            [parenthesin.components.http :as components.http]
            [parenthesin.interceptors :as interceptors]
            [schema.core :as s]
            [schema.test :as schema.test]
            [state-flow.api :refer [defflow]]
            [state-flow.assertions.matcher-combinators :refer [match?]]
            [state-flow.core :as state-flow :refer [flow]]))

(clojure.test/use-fixtures :once schema.test/validate-schemas)

(defn do-deposit!
  [{{:keys [btc]} :body
    {:keys [http database]} :components}]
  (let [response (-> http
                     (components.http/request {:url "http://coinbase.org" :method :get})
                     :body
                     (json/decode true))
        rate (:rate response)
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
  {:interceptors interceptors/base-interceptors
   :routes [{:path "/wallet/deposit"
             :method :post
             :parameters {:body {:btc s/Num}}
             :responses {201 {:body {:usd s/Num}}}
             :handler do-deposit!}
            {:path "/wallet/list"
             :method :get
             :responses {200 {:body [{:id s/Int :amount BigDecimal}]}}
             :handler get-wallet}]})

(defflow
  flow-integration-system-test
  {:init (partial aux/start-system! test-routes)
   :cleanup aux/stop-system!
   :fail-fast? true}
  (flow "should interact with system"

    (flow "prepare system with http-out mocks and creating tables"
      (util.http/set-http-out-responses! {"http://coinbase.org" {:body {:rate 35000.0M}
                                                                 :status 200}})

      (util.database/execute! ["create table if not exists wallet (
                                  id serial primary key,
                                  price decimal)"])

      (flow "should insert deposit into wallet"
        (match? {:status 201
                 :body {:usd 70000.0}}
                (util.webserver/request! {:method :post
                                          :uri    "/wallet/deposit"
                                          :body   {:btc 2M}})))

      (flow "should list wallet deposits"
        (match? {:status 200
                 :body [{:id 1
                         :amount 70000.0M}]}
                (util.webserver/request! {:method :get
                                          :uri    "/wallet/list"}))))))
