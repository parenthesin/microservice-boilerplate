(ns integration.microservice-boilerplate.wallet-test
  (:require [clojure.test :as clojure.test]
            [com.stuartsierra.component :as component]
            [integration.microservice-boilerplate.util :as util]
            [matcher-combinators.matchers :as matchers]
            [microservice-boilerplate.routes :as routes]
            [parenthesin.components.config.aero :as components.config]
            [parenthesin.components.db.jdbc-hikari :as components.database]
            [parenthesin.components.http.clj-http :as components.http]
            [parenthesin.components.router.reitit-schema :as components.router]
            [parenthesin.components.server.reitit-pedestal-jetty :as components.webserver]
            [parenthesin.helpers.state-flow.http :as state-flow.http]
            [parenthesin.helpers.state-flow.server.pedestal :as state-flow.server]
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
    :router (components.router/new-router routes/routes)
    :database (component/using (components.database/new-database)
                               [:config])
    :webserver (component/using (components.webserver/new-webserver)
                                [:config :http :router :database]))))

(defflow
  flow-integration-wallet-test
  {:init (util/start-system! create-and-start-components!)
   :cleanup util/stop-system!
   :fail-fast? true}
  (flow "should interact with system"

    (flow "prepare system with http-out mocks"
      (state-flow.http/set-http-out-responses! {"https://api.kraken.com/0/public/Ticker?pair=XBTUSD"
                                                {:body {:result {:XXBTZUSD {:c [30000.00 0.01]}}}
                                                 :status 200}})

      (flow "should get current btc / usd pair"
        (match? (matchers/embeds {:status 200
                                  :body  {:btc-amount 1
                                          :usd-amount 30000.00}})

                (state-flow.server/request! {:method :get
                                             :uri    "/wallet/current-btc-usd"})))

      (flow "should insert deposit into wallet"
        (match? (matchers/embeds {:status 201
                                  :body  {:id string?
                                          :btc-amount 2
                                          :usd-amount-at 60000.0}})
                (state-flow.server/request! {:method :post
                                             :uri    "/wallet/deposit"
                                             :body   {:btc 2M}})))

      (flow "should insert withdrawal into wallet"
        (match? (matchers/embeds {:status 201
                                  :body  {:id string?
                                          :btc-amount -1
                                          :usd-amount-at -30000.0}})
                (state-flow.server/request! {:method :post
                                             :uri    "/wallet/withdrawal"
                                             :body   {:btc -1M}})))

      (flow "shouldn't insert deposit negative values into wallet"
        (match? {:status 400
                 :body  "btc deposit amount can't be negative."}
                (state-flow.server/request! {:method :post
                                             :uri    "/wallet/deposit"
                                             :body   {:btc -2M}})))

      (flow "shouldn't insert withdrawal positive values into wallet"
        (match? {:status 400
                 :body  "btc withdrawal amount can't be positive."}
                (state-flow.server/request! {:method :post
                                             :uri    "/wallet/withdrawal"
                                             :body   {:btc 2M}})))

      (flow "shouldn't insert withdrawal into wallet"
        (match? {:status 400
                 :body  "withdrawal amount bigger than the total in the wallet."}
                (state-flow.server/request! {:method :post
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
                (state-flow.server/request! {:method :get
                                             :uri    "/wallet/history"}))))))
