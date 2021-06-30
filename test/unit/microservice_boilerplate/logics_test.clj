(ns unit.microservice-boilerplate.logics-test
  (:require [clojure.test :refer [deftest is testing use-fixtures]]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.properties :as properties]
            [matcher-combinators.matchers :as matchers]
            [matcher-combinators.test :refer [match?]]
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

(def wallet-entry-1
  #:wallet{:id #uuid "ecdcf860-0c2a-3abf-9af1-a70e770cea9a"
           :btc_amount 3
           :usd_amount_at 34000M
           :created_at #inst "2020-10-23T00:00:00"})

(def wallet-entry-2
  #:wallet{:id #uuid "67272ecc-b839-37e3-9656-2895d1f0fda2"
           :btc_amount -1
           :usd_amount_at 33000M
           :created_at #inst "2020-10-24T00:00:00"})

(def wallet-entry-3
  #:wallet{:id #uuid "f4259476-efe4-3a26-ad30-1dd0ffd49fc3"
           :btc_amount -1
           :usd_amount_at 32000M
           :created_at #inst "2020-10-25T00:00:00"})

(def wallet-entry-4
  #:wallet{:id #uuid "0d93f041-eae4-3af9-b5e1-f9ee844e82d9"
           :btc_amount 1
           :usd_amount_at 36000M
           :created_at #inst "2020-10-26T00:00:00"})

(def wallet-entries [wallet-entry-1 wallet-entry-2 wallet-entry-3 wallet-entry-4])

(deftest ->wallet-history-test
  (testing "should reduce and get totals for wallet entries and current usd"
    (is (match? {:entries (matchers/embeds [{:id uuid?
                                             :btc-amount number?
                                             :usd-amount-at number?
                                             :created-at inst?}])
                 :total-btc 2M
                 :total-current-usd 60000M}
                (logics/->wallet-history 30000M wallet-entries)))))
