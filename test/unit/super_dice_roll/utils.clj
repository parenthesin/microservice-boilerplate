(ns unit.super-dice-roll.utils 
  (:require [schema-generators.generators :as g]
            [super-dice-roll.schemas.models :as schemas.models]))

(defn ->cmd [command]
  (assoc
   (g/generate schemas.models/RollCommand)
    :command command))

(defn ->roll [roll]
  (merge
   (g/generate schemas.models/Roll)
   roll))

(defn in-range?
  ([maximum]
   (in-range? 1 maximum))
  ([minimun maximum]
   (fn [value] (and (>= value minimun) (<= value maximum)))))
