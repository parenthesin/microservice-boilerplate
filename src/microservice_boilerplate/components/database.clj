(ns microservice-boilerplate.components.database
  (:require [clojure.tools.logging :as log]
            [com.stuartsierra.component :as component]
            [next.jdbc :as jdbc]
            [next.jdbc.connection :as connection])
  (:import (com.zaxxer.hikari HikariDataSource)))

(defprotocol DatabaseProvider
  (execute [self command]
    "Low-level API to execute a command in the database"))

(defrecord Database [config ^HikariDataSource datasource]
  component/Lifecycle
  (start [this]
    (let [{:keys [host port dbtype] :as db-spec} (get-in config [:config :database])]
      (log/info :database :start {:host host :port port :dbtype dbtype})
      (if datasource
        this
        (assoc this :datasource (connection/->pool HikariDataSource db-spec)))))
  (stop [this]
    (log/info :database :stop)
    (if datasource
      (do
        (.close datasource)
        (assoc this :datasource nil))
      this))

  DatabaseProvider
  (execute [this commands]
    (jdbc/execute! (:datasource this) commands)))

(defn new-database []
  (map->Database {}))
