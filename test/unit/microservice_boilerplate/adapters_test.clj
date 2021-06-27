(ns unit.microservice-boilerplate.adapters-test
  (:require [clojure.test :refer [deftest is testing use-fixtures]]
            [matcher-combinators.test :refer [match?]]
            [microservice-boilerplate.adapters :as adapters]
            [schema.test :as schema.test]))

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
