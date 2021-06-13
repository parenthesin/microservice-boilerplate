(ns microservice-boilerplate.system
  (:require [com.stuartsierra.component :as component]))

(def system-atom (atom nil))

(defn start-system! [system-map]
  (->> system-map
       component/start
       (reset! system-atom)))

#_{:clj-kondo/ignore [:unused-public-var]}
(defn stop-system! []
  (swap!
   system-atom
   (fn [s] (when s (component/stop s)))))
