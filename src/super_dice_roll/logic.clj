(ns super-dice-roll.logic
  (:require [clojure.string :as string]
            [schema.core :as s]
            [super-dice-roll.schemas.models :as schemas.models]))

(defn int-or-arg [input arg]
  (try 
    (Integer/parseInt input)
    (catch Exception _
      arg)))

(defn parse-first-part
  [command]
  (-> command
      string/upper-case
      (string/split #"D")))

(defn parse-modifiers [value modifier]
  (let [parsed (string/split value (re-pattern (str "\\" modifier)))
        sign (if (= modifier \+) 1 -1)]
    {:dice (-> parsed first (int-or-arg 0))
     :modifier (-> parsed last not-empty (or 0) Integer/parseInt (* sign))}))

(defn parse-second-part
  [command]
  (cond
    (string/index-of command \+) (parse-modifiers command \+)
    (string/index-of command \-) (parse-modifiers command \-)
    :else {:dice (int-or-arg command 0) :modifier 0}))

(s/defn roll-command->roll :- schemas.models/Roll
  [{:keys [command] :as roll-command} :- schemas.models/RollCommand]
  (let [parse (parse-first-part command)
        times (-> parse first (int-or-arg 1))
        second-part (parse-second-part (last parse))]
    (merge {:command roll-command :times times} second-part)))

(roll-command->roll {:user {:id "aaa" :username "bbb" :nick "ccc"} :command "6d12+4"})
