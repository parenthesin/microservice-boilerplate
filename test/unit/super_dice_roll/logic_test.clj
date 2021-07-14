(ns unit.super-dice-roll.logic-test
  (:require [clojure.test :refer [deftest are testing use-fixtures]]
            [matcher-combinators.test :refer [match?]]
            [schema-generators.generators :as g]
            [schema.test :as schema.test]
            [super-dice-roll.logic :as logic]
            [super-dice-roll.schemas.models :as schemas.models]))

(use-fixtures :once schema.test/validate-schemas)

(defn- ->cmd [command]
  (assoc
   (g/generate schemas.models/RollCommand)
    :command command))

(deftest roll-command->roll-test
  (testing "should parse commands into roll map to be rolled afterwards"
    (are [command expected] (match? expected (logic/roll-command->roll (->cmd command)))
      "d6"     {:times 01 :dice 06 :modifier  0}
      "2d12"   {:times 02 :dice 12 :modifier  0}
      "3d20+5" {:times 03 :dice 20 :modifier  5}
      "15d4-5" {:times 15 :dice 04 :modifier -5}
      "wololo" {:times 01 :dice 00 :modifier  0}
      ""       {:times 01 :dice 00 :modifier  0})))
