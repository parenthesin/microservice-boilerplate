(ns super-dice-roll.controllers
  (:require [schema.core :as s]
            [super-dice-roll.schemas.models :as schemas.models]))

(defn- rand-int-range [a b]
  (int (+ a (rand (- b a)))))

(s/defn roll->rolled :- schemas.models/Rolled
  [{:keys [times dice modifier] :as roll} :- schemas.models/Roll]
  (let [results (mapv (fn [_] (rand-int-range 1 (inc dice)))
                      (range 0 times))]
    {:roll roll
     :results results
     :total  (-> (reduce + results) (+ modifier))}))
