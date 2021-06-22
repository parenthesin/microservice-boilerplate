(ns parenthesin.components.config
  (:require [com.stuartsierra.component :as component]
            [clojure.java.io :as io]
            [aero.core :as aero]))

(def current-profile (keyword (or (System/getenv "SYSTEM_ENV") "dev")))

(defn config [profile]
  (aero/read-config (clojure.java.io/resource "config.edn")
                    {:profile profile}))

(defrecord Config [config]
  component/Lifecycle
  (start [this] this)
  (stop  [this] this))

(defn new-config
  ([]
   (new-config {}))
  ([input-map]
   (map->Config {:config (merge (config current-profile)
                                {:env current-profile}
                                input-map)})))
