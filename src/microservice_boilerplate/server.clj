(ns microservice-boilerplate.server
  (:require [com.stuartsierra.component :as component]
            [microservice-boilerplate.routes :as routes]
            [parenthesin.components.config :as config]
            [parenthesin.components.database :as database]
            [parenthesin.components.http :as http]
            [parenthesin.components.router :as router]
            [parenthesin.components.webserver :as webserver]
            [parenthesin.logs :as logs]
            [parenthesin.migrations :as migrations])
  (:gen-class))

(def system-atom (atom nil))

(defn- build-system-map []
  (component/system-map
   :config (config/new-config)
   :http (http/new-http)
   :router (router/new-router routes/routes)
   :database (component/using (database/new-database) [:config])
   :webserver (component/using (webserver/new-webserver) [:config :http :router :database])))

(defn start-system! [system-map]
  (logs/setup :info :auto)
  (migrations/migrate (migrations/configuration-with-db))
  (->> system-map
       component/start
       (reset! system-atom)))

#_{:clj-kondo/ignore [:unused-public-var]}
(defn stop-system! []
  (swap!
   system-atom
   (fn [s] (when s (component/stop s)))))

(defn -main
  "The entry-point for 'gen-class'"
  [& _args]
  (start-system! (build-system-map)))

(comment
  (stop-system!))
