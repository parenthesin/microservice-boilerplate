(ns integration.microservice-boilerplate.wallet-test
  (:require [clojure.test :as clojure.test]
            [com.stuartsierra.component :as component]
            [integration.microservice-boilerplate.aux :as aux]
            [integration.parenthesin.aux.http :as aux.http]
            [integration.parenthesin.aux.webserver :as aux.webserver]
            [matcher-combinators.matchers :as matchers]
            [microservice-boilerplate.routes :as routes]
            [parenthesin.components.config :as components.config]
            [parenthesin.components.database :as components.database]
            [parenthesin.components.http :as components.http]
            [parenthesin.components.router :as components.router]
            [parenthesin.components.webserver :as components.webserver]
            [schema.test :as schema.test]
            [state-flow.api :refer [defflow]]
            [state-flow.assertions.matcher-combinators :refer [match?]]
            [state-flow.core :as state-flow :refer [flow]]
            [state-flow.state :as state]))

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
  {:init (aux/start-system! create-and-start-components!)
   :cleanup aux/stop-system!
   :fail-fast? true}
  (flow "should interact with system"

    (flow "prepare system with http-out mocks"
      (aux.http/set-http-out-responses! {"https://api.coindesk.com/v1/bpi/currentprice.json"
                                         {:body {:bpi {:USD {:rate_float 30000.00}}}
                                          :status 200}})

      (flow "should insert deposit into wallet"
        (match? (matchers/embeds {:status 201
                                  :body  {:id string?
                                          :btc-amount 2
                                          :usd-amount-at 60000.0}})
                (aux.webserver/request! {:method :post
                                         :uri    "/wallet/deposit"
                                         :body   {:btc 2M}})))

      ;(flow "should list wallet deposits"
        ;(match? {:status 200
                 ;:body [{:id 1
                         ;:amount 70000.0}]}
                ;(aux.webserver/request! {:method :get
                                         ;:uri    "/wallet/list"})))
      )))
