(ns unit.parenthesin.components.http-test
  (:require [clojure.test :refer [deftest is testing]]
            [com.stuartsierra.component :as component]
            [matcher-combinators.test :refer [match?]]
            [parenthesin.components.http :as components.http]))

(defn- create-and-start-system!
  [{:keys [http]}]
  (component/start-system
   (component/system-map :http http)))

(deftest http-mock-component-test
  (testing "HttpMock should return mocked reponses and log requests in the atom"
    (let [system (create-and-start-system!
                  {:http (components.http/new-http-mock
                          {"https://duckduckgo.com" {:status 200}})})]

      (is (match? {:status 200}
                  (components.http/request (:http system) {:url "https://duckduckgo.com"})))

      (is (match? {:status 500}
                  (components.http/request (:http system) {:url "https://google.com"})))

      (is (match? ["https://duckduckgo.com"
                   "https://google.com"]
                  (map :url (deref (get-in system [:http :requests]))))))))
