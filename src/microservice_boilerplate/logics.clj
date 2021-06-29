(ns microservice-boilerplate.logics
  (:require [microservice-boilerplate.adapters :as adapters]
            [schema.core :as s])
  (:import [java.util UUID]))

(s/defn uuid-from-string :- s/Uuid
  [seed :- s/Str]
  (-> seed
      .getBytes
      UUID/nameUUIDFromBytes))

(s/defn uuid-from-date-amount :- s/Uuid
  [date :- s/Inst
   amount :- s/Num]
  (-> date
    (adapters/inst->utc-formated-string "yyyy-MM-dd hh:mm:ss")
    (str amount)
    uuid-from-string))
