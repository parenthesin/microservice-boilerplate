(ns integration.microservice-boilerplate.wallet-test
  (:require [clojure.test :as clojure.test]
            [com.stuartsierra.component :as component]
            [integration.microservice-boilerplate.util :as util]
            [integration.parenthesin.util.http :as util.http]
            [integration.parenthesin.util.webserver :as util.webserver]
            [matcher-combinators.matchers :as matchers]
            [microservice-boilerplate.routes :as routes]
            [parenthesin.components.config :as components.config]
            [parenthesin.components.database :as components.database]
            [parenthesin.components.http :as components.http]
            [parenthesin.components.webserver :as components.webserver]
            [schema.test :as schema.test]
            [state-flow.api :refer [defflow]]
            [state-flow.assertions.matcher-combinators :refer [match?]]
            [state-flow.core :as state-flow :refer [flow]]))

(clojure.test/use-fixtures :once schema.test/validate-schemas)

(defn- create-and-start-components! []
  (component/start-system
   (component/system-map
    :config (components.config/new-config)
    :http (components.http/new-http-mock {})
    :database (component/using (components.database/new-database)
                               [:config])
    :webserver (component/using (components.webserver/new-webserver routes/routes)
                                [:config :http :database]))))

(defflow
  flow-integration-wallet-test
  {:init (util/start-system! create-and-start-components!)
   :cleanup util/stop-system!
   :fail-fast? true}
  (flow "should interact with system"

    (flow "prepare system with http-out mocks"
      (util.http/set-http-out-responses! {"https://api.coindesk.com/v1/bpi/currentprice.json"
                                          {:body {:bpi {:USD {:rate_float 30000.00}}}
                                           :status 200}})

      (flow "should insert deposit into wallet"
        (match? (matchers/embeds {:status 201
                                  :body  {:id string?
                                          :btc-amount 2
                                          :usd-amount-at 60000.0}})
                (util.webserver/request! {:method :post
                                          :uri    "/wallet/deposit"
                                          :body   {:btc 2M}})))

      (flow "should insert withdrawal into wallet"
        (match? (matchers/embeds {:status 201
                                  :body  {:id string?
                                          :btc-amount -1
                                          :usd-amount-at -30000.0}})
                (util.webserver/request! {:method :post
                                          :uri    "/wallet/withdrawal"
                                          :body   {:btc -1M}})))

      (flow "shouldn't insert deposit negative values into wallet"
        (match? {:status 400
                 :body  "btc deposit amount can't be negative."}
                (util.webserver/request! {:method :post
                                          :uri    "/wallet/deposit"
                                          :body   {:btc -2M}})))

      (flow "shouldn't insert withdrawal positive values into wallet"
        (match? {:status 400
                 :body  "btc withdrawal amount can't be positive."}
                (util.webserver/request! {:method :post
                                          :uri    "/wallet/withdrawal"
                                          :body   {:btc 2M}})))

      (flow "shouldn't insert withdrawal into wallet"
        (match? {:status 400
                 :body  "withdrawal amount bigger than the total in the wallet."}
                (util.webserver/request! {:method :post
                                          :uri    "/wallet/withdrawal"
                                          :body   {:btc -2M}})))

      (flow "should list wallet deposits"
        (match? (matchers/embeds {:status 200
                                  :body {:entries [{:id string?
                                                    :btc-amount 2
                                                    :usd-amount-at 60000.0
                                                    :created-at string?}
                                                   {:id string?
                                                    :btc-amount -1
                                                    :usd-amount-at -30000.0
                                                    :created-at string?}]
                                         :total-btc 1
                                         :total-current-usd 30000.0}})
                (util.webserver/request! {:method :get
                                          :uri    "/wallet/history"}))))))
