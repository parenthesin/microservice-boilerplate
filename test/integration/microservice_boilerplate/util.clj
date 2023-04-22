(ns integration.microservice-boilerplate.util
  (:require [com.stuartsierra.component :as component]
            [parenthesin.helpers.logs :as logs]
            [parenthesin.helpers.migrations :as migrations]
            [pg-embedded-clj.core :as pg-emb]))

(defn start-system!
  [system-start-fn]
  (fn []
    (logs/setup :info :auto)
    (pg-emb/init-pg)
    (migrations/migrate (migrations/configuration-with-db))
    (system-start-fn)))

(defn stop-system!
  [system]
  (component/stop-system system)
  (pg-emb/halt-pg!))
