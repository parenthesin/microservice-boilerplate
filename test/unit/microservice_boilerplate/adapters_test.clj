(ns unit.microservice-boilerplate.adapters-test
  (:require [clojure.test :refer [deftest is testing use-fixtures]]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.properties :as properties]
            [matcher-combinators.matchers :as matchers]
            [matcher-combinators.test :refer [match?]]
            [microservice-boilerplate.adapters :as adapters]
            [microservice-boilerplate.schemas.db :as schemas.db]
            [microservice-boilerplate.schemas.types :as schemas.types]
            [schema-generators.generators :as g]
            [schema.core :as s]
            [schema.test :as schema.test]
            [microservice-boilerplate.schemas.wire-in :as schemas.wire-in]))

(use-fixtures :once schema.test/validate-schemas)

(deftest inst->formated-string
  (testing "should adapt clojure/instant to formated string"
    (is (= "1987-02-10 09:38:43"
           (adapters/inst->utc-formated-string #inst "1987-02-10T09:38:43.000Z"
                                               "yyyy-MM-dd hh:mm:ss")))))

(def coindesk-response-fixture
  {:time {:updated "Jun 26, 2021 20:06:00 UTC"
          :updatedISO "2021-06-26T20:06:00+00:00"
          :updateduk "Jun 26, 2021 at 21:06 BST"}
   :bpi {:USD
         {:code "USD"
          :symbol "&#36;"
          :rate "31,343.9261"
          :description "United States Dollar"
          :rate_float 31343.9261}
         :GBP
         {:code "GBP"
          :symbol "&pound;"
          :rate "22,573.9582"
          :description "British Pound Sterling"
          :rate_float 22573.9582}}})

(deftest wire->usd-price-test
  (testing "should adapt coindesk response into a number"
    (is (match? 31343.9261M
                (adapters/wire->usd-price coindesk-response-fixture)))))

(defspec wire-in-db-test 50
  (properties/for-all [id (g/generator s/Uuid)
                       pos-num (g/generator schemas.types/PositiveNumber schemas.types/TypesLeafGenerators)
                       neg-num (g/generator schemas.types/NegativeNumber schemas.types/TypesLeafGenerators)]
    (s/validate schemas.db/WalletTransaction (adapters/withdrawal->db id neg-num pos-num))
    (s/validate schemas.db/WalletTransaction (adapters/deposit->db id pos-num pos-num))))

(defspec db-wire-in-test 50
  (properties/for-all [wallet-db (g/generator schemas.db/WalletEntry)]
    (s/validate schemas.wire-in/WalletEntry (adapters/db->wire-in wallet-db))))

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
                (adapters/->wallet-history 30000M wallet-entries)))))
