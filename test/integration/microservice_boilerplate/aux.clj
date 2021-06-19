(ns integration.microservice-boilerplate.aux
  (:require [com.stuartsierra.component :as component]
            [microservice-boilerplate.components.config :as components.config]
            [microservice-boilerplate.components.database :as components.database]
            [microservice-boilerplate.components.http :as components.http]
            [microservice-boilerplate.components.webserver :as components.webserver]
            [pg-embedded-clj.core :as pg-emb]
            [state-flow.core :refer [default-stack-trace-exclusions filter-stack-trace log-error throw-error!]]))

(defn- create-and-start-system! []
  (component/start-system
   (component/system-map
     :config (components.config/new-config)
     :http (components.http/new-http-mock {})
     :database (component/using (components.database/new-database) [:config])
     :webserver (component/using (components.webserver/new-webserver) [:config :http :database]))))

(defn build-initial-state []
  (pg-emb/init-pg)
  (create-and-start-system!))

(defn stop-system!
  [system]
  (component/stop-system system)
  (pg-emb/halt-pg!))

(defmacro init-flow
  [flow-name & flows]
  `(state-flow.api/defflow ~flow-name {:init build-initial-state
                                       :fail-fast? true
                                       :cleanup stop-system!
                                       :on-error (comp stop-system!
                                                       throw-error!
                                                       log-error
                                                       (filter-stack-trace default-stack-trace-exclusions))}
     ~@flows))
