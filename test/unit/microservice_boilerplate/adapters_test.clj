(ns unit.microservice-boilerplate.adapters-test
  (:require [clojure.test :refer [deftest is testing use-fixtures]]
            [matcher-combinators.test :refer [match?]]
            [microservice-boilerplate.adapters :as adapters.price]
            [schema.test :as schema.test]))

(use-fixtures :once schema.test/validate-schemas)

(def coindesk-response-fixture
  {:time
   {:updated "Jun 26, 2021 20:06:00 UTC",
    :updatedISO "2021-06-26T20:06:00+00:00",
    :updateduk "Jun 26, 2021 at 21:06 BST"},
   :disclaimer
   "This data was produced from the CoinDesk Bitcoin Price Index (USD). Non-USD currency data converted using hourly conversion rate from openexchangerates.org",
   :chartName "Bitcoin",
   :bpi
   {:USD
    {:code "USD",
     :symbol "&#36;",
     :rate "31,343.9261",
     :description "United States Dollar",
     :rate_float 31343.9261},
    :GBP
    {:code "GBP",
     :symbol "&pound;",
     :rate "22,573.9582",
     :description "British Pound Sterling",
     :rate_float 22573.9582},
    :EUR
    {:code "EUR",
     :symbol "&euro;",
     :rate "26,259.7845",
     :description "Euro",
     :rate_float 26259.7845}}})

(deftest wire->usd-price-test
  (testing "should adapt coindesk response into a number"
    (is (match? 31343.9261M
                (adapters.price/wire->usd-price coindesk-response-fixture)))))
