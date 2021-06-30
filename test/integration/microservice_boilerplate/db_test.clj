(ns integration.microservice-boilerplate.db-test
  (:require [clojure.test :as clojure.test]
            [com.stuartsierra.component :as component]
            [microservice-boilerplate.db :as db]
            [migrations.core :as migrations]
            [parenthesin.components.config :as components.config]
            [parenthesin.components.database :as components.database]
            [pg-embedded-clj.core :as pg-emb]
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
    :database (component/using (components.database/new-database)
                               [:config]))))

(defn start-system! []
  (pg-emb/init-pg)
  (migrations/migrate (migrations/configuration-with-db))
  (create-and-start-components!))

(defn stop-system!
  [system]
  (component/stop-system system)
  (pg-emb/halt-pg!))

(defflow
  flow-integration-db-test
  {:init start-system!
   :cleanup stop-system!
   :fail-fast? true}
  (flow "creates a table, insert data and checks return in the database"
    [database (state-flow.api/get-state :database)]

    (state/invoke
     #(db/insert-wallet-transaction {:wallet/id #uuid "cd989358-af38-4a2f-a1a1-88096aa425a7"
                                     :wallet/btc_amount 2.0M
                                     :wallet/usd_amount_at 66000.00M}
                                    database))

    (flow "check transaction was inserted in db"
      (match? [#:wallet{:id #uuid "cd989358-af38-4a2f-a1a1-88096aa425a7"
                        :btc_amount 2.0M
                        :usd_amount_at 66000.00M
                        :created_at inst?}]
              (db/get-wallet-all-transactions database)))))
