(ns unit.microservice-boilerplate.logics-test
  (:require [clojure.test :refer [are deftest is testing use-fixtures]]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.properties :as properties]
            [microservice-boilerplate.adapters :as adapters]
            [microservice-boilerplate.logics :as logics]
            [microservice-boilerplate.schemas.db :as schemas.db]
            [microservice-boilerplate.schemas.types :as schemas.types]
            [schema-generators.generators :as g]
            [schema.core :as s]
            [schema.test :as schema.test]))

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

(deftest uuid-from-date-amount-test
  (testing "should generate the same uuid based on inputs"

    (is (= #uuid "ecdcf860-0c2a-3abf-9af1-a70e770cea9a"
           (logics/uuid-from-date-amount #inst "2020-10-23T22:30:34" 123.00M)))

    (is (= #uuid "67272ecc-b839-37e3-9656-2895d1f0fda2"
           (logics/uuid-from-date-amount #inst "2020-10-23T22:30:34" -123.00M)))))

(defspec wallet-entry-test 50
  (properties/for-all [date (g/generator s/Inst)
                       pos-num (g/generator schemas.types/PositiveNumber schemas.types/TypesLeafGenerators)
                       neg-num (g/generator schemas.types/NegativeNumber schemas.types/TypesLeafGenerators)]
    (s/validate schemas.db/WalletTransaction (logics/->wallet-transaction date neg-num pos-num))))


(deftest can-withdrawal-test
  (testing "checks can-withdrawal? logic"
    (are [x y] (= x y)
      true (logics/can-withdrawal? -0.9M 1.0M)
      true (logics/can-withdrawal? -1.0M 1.0M)
      false (logics/can-withdrawal? -1.1M 1.0M))))
