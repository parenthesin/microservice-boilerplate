(ns unit.super-dice-roll.logics-test
  (:require [clojure.test :refer [are deftest testing use-fixtures]]
            [matcher-combinators.test :refer [match?]]
            [schema.test :as schema.test]
            [super-dice-roll.logics :as logic]
            [unit.super-dice-roll.utils :refer [->cmd ->roll]]))

(use-fixtures :once schema.test/validate-schemas)

(deftest roll-command->roll-test
  (testing "should parse commands into roll map to be rolled afterwards"
    (are [command expected] (match? expected (logic/roll-command->roll (->cmd command)))
      "d6"     {:times 01 :dice 06 :modifier  0}
      "2d12"   {:times 02 :dice 12 :modifier  0}
      "3d20+5" {:times 03 :dice 20 :modifier  5}
      "15d4-5" {:times 15 :dice 04 :modifier -5}
      "wololo" {:times 01 :dice 00 :modifier  0}
      ""       {:times 01 :dice 00 :modifier  0})))

(deftest valid-roll-test
  (testing "should validate times amount and dices sizes"
    (are [roll expected] (= expected (logic/valid-roll? (->roll roll)))
      {:times 1    :dice 6    :modifier  0} true
      {:times 3    :dice 20   :modifier  5} true
      {:times 15   :dice 4    :modifier -5} true
      {:times 100  :dice 1000 :modifier  0} true
      {:times 1    :dice 1001 :modifier  0} false
      {:times 1    :dice 0    :modifier  0} false
      {:times 1001 :dice 6    :modifier  0} false
      {:times 0    :dice 6    :modifier  0} false)))
