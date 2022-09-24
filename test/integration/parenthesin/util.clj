(ns integration.parenthesin.util
  (:require [com.stuartsierra.component :as component]
            [parenthesin.components.config :as components.config]
            [parenthesin.components.database :as components.database]
            [parenthesin.components.http :as components.http]
            [parenthesin.components.webserver :as components.webserver]
            [parenthesin.logs :as logs]
            [pg-embedded-clj.core :as pg-emb]))

(defn- create-and-start-components! [routes]
  (component/start-system
   (component/system-map
    :config (components.config/new-config)
    :http (components.http/new-http-mock {})
    :database (component/using (components.database/new-database)
                               [:config])
    :webserver (component/using (components.webserver/new-webserver routes)
                                [:config :http :database]))))

(defn start-system!
  ([]
   (start-system! []))
  ([routes]
   (logs/setup [["*" :debug]] :auto)
   (pg-emb/init-pg)
   (create-and-start-components! routes)))

(defn stop-system!
  [system]
  (component/stop-system system)
  (pg-emb/halt-pg!))
