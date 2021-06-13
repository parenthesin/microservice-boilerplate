(ns microservice-boilerplate.server
  (:require [com.stuartsierra.component :as component]
            [microservice-boilerplate.components.config :as config]
            [microservice-boilerplate.components.router :as router]
            [microservice-boilerplate.components.webserver :as webserver]
            [microservice-boilerplate.routes :as routes]
            [microservice-boilerplate.system :as system])
  (:gen-class))

(defn- build-system-map []
  (component/system-map
   :config (config/new-config)
   :router (router/new-router routes/routes)
   :webserver (component/using (webserver/new-webserver) [:config :router])))

(defn -main
  "The entry-point for 'gen-class'"
  [& _args]
  (system/start-system! (build-system-map)))

(comment
  (system/stop-system!))
