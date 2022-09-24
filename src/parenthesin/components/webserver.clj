(ns parenthesin.components.webserver
  (:require [com.stuartsierra.component :as component]
            [org.httpkit.server :as http-kit]
            [parenthesin.interoute :refer [routes->handler]]
            [parenthesin.logs :as logs]))

(defrecord WebServer [config routes interceptors]
  component/Lifecycle
  (start [this]
    (let [{:webserver/keys [port]
           :keys [env]} (:config config)
          ctx {:interceptors interceptors
               :components this}
          route-handlers (routes->handler routes ctx)]
      (logs/log :info :webserver :start {:env env :port port})
      (assoc this
             :route-handlers route-handlers
             :webserver (http-kit/run-server route-handlers {:port port}))))

  (stop [this]
    (let [server-fn (:webserver this)]
      (logs/log :info :webserver :stop)
      (server-fn :timeout 100)
      (dissoc this :webserver)
      this)))

(defn new-webserver [{:keys [routes interceptors]}]
  (map->WebServer {:routes routes
                   :interceptors interceptors}))
