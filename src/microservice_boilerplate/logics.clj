(ns microservice-boilerplate.logics
  (:require [schema.core :as s])
  (:import [java.util UUID]))

(s/defn uuid-from-string :- s/Uuid
  [seed :- s/Str]
  (-> seed
      .getBytes
      UUID/nameUUIDFromBytes))
