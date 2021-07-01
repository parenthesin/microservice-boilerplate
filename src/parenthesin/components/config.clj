(ns parenthesin.components.config
  (:require [aero.core :as aero]
            [clojure.java.io :as io]
            [com.stuartsierra.component :as component]))

(def ^:private current-profile (keyword (or (System/getenv "SYSTEM_ENV") "dev")))

(defn- config [profile]
  (aero/read-config (clojure.java.io/resource "config.edn")
                    {:profile profile}))

(defn read-config [extra-inputs]
  (merge (config current-profile)
         {:env current-profile}
         extra-inputs))

(defrecord Config [config]
  component/Lifecycle
  (start [this] this)
  (stop  [this] this))

(defn new-config
  ([]
   (new-config {}))
  ([input-map]
   (map->Config {:config (read-config input-map)})))
