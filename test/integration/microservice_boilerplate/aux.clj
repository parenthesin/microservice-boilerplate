(ns integration.microservice-boilerplate.aux
  (:require [com.stuartsierra.component :as component]
            [microservice-boilerplate.components.config :as components.config]
            [microservice-boilerplate.components.database :as components.database]
            [microservice-boilerplate.components.http :as components.http]
            [microservice-boilerplate.components.router :as components.router]
            [microservice-boilerplate.components.webserver :as components.webserver]
            [pg-embedded-clj.core :as pg-emb]))

(defn- create-and-start-components! [routes]
  (component/start-system
   (component/system-map
     :config (components.config/new-config)
     :http (components.http/new-http-mock {})
     :router (components.router/new-router routes)
     :database (component/using (components.database/new-database)
                                [:config])
     :webserver (component/using (components.webserver/new-webserver)
                                 [:config :http :router :database]))))

(defn start-system!
  ([]
   (start-system! []))
  ([routes]
   (pg-emb/init-pg)
   (create-and-start-components! routes)))

(defn stop-system!
  [system]
  (component/stop-system system)
  (pg-emb/halt-pg!))
