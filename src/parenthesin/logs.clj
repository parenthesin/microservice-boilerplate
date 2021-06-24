(ns parenthesin.logs
  (:require [taoensso.timbre :as timbre]
            [taoensso.timbre.appenders.core :as core-appenders]))

(defn setup [level stream]
  (timbre/set-level! level)
  (timbre/merge-config!
   {:appenders
    {:println
     (core-appenders/println-appender {:stream stream})}}))

(defn log [level & args]
  (timbre/log level args))
