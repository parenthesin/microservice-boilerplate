(ns unit.microservice-boilerplate.logics-test
  (:require [clojure.test :refer [deftest is testing use-fixtures]]
            [microservice-boilerplate.logics :as logics]
            [schema.test :as schema.test]
            [microservice-boilerplate.adapters :as adapters]))

(use-fixtures :once schema.test/validate-schemas)

(deftest uuid-from-string-test
  (testing "should generate the same uuid based on the seeded string"
    (is (= #uuid "c9fcd170-cdc4-3359-a3ed-d24439361c84"
           (logics/uuid-from-string "boletaria"))
        "uuid from random string")

    (is (= #uuid "0d93f041-eae4-3af9-b5e1-f9ee844e82d9"
           (logics/uuid-from-string
            (str 100.00M
                 (adapters/inst->utc-formated-string #inst "2021-11-23T22:30:34"
                                                     "yyyy-MM-dd hh:mm:ss"))))
        "uuid from string with debit amount and date")

    (is (= #uuid "f4259476-efe4-3a26-ad30-1dd0ffd49fc3"
           (logics/uuid-from-string
            (str -100.00M
                 (adapters/inst->utc-formated-string #inst "2021-11-23T22:30:34"
                                                     "yyyy-MM-dd hh:mm:ss"))))
        "uuid from string with withdrawal amount and date")))
