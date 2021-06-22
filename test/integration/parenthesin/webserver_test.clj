(ns integration.parenthesin.webserver-test
  (:require [clojure.test :as clojure.test]
            [integration.parenthesin.aux :as aux]
            [integration.parenthesin.aux.webserver :as aux.webserver]
            [schema.core :as s]
            [schema.test :as schema.test]
            [state-flow.api :refer [defflow]]
            [state-flow.assertions.matcher-combinators :refer [match?]]
            [state-flow.core :as state-flow :refer [flow]]))

(clojure.test/use-fixtures :once schema.test/validate-schemas)

(def test-routes
  [["/plus"
    {:get {:summary "plus with spec query parameters"
           :parameters {:query {:x s/Int, :y s/Int}}
           :responses {200 {:body {:total s/Int}}}
           :handler (fn [{{{:keys [x y]} :query} :parameters}]
                      {:status 200
                       :body {:total (+ x y)}})}
     :post {:summary "plus with spec body parameters"
            :parameters {:body {:x s/Int, :y s/Int}}
            :responses {200 {:body {:total s/Int}}}
            :handler (fn [{{{:keys [x y]} :body} :parameters}]
                       {:status 200
                        :body {:total (+ x y)}})}}]])

(defflow
  flow-integration-webserver-test
  {:init (partial aux/start-system! test-routes)
   :cleanup aux/stop-system!
   :fail-fast? true}
  (flow "should interact test-routes"
    (flow "should sum the get params x & y via get"
      (match? {:status 200
               :body {:total 7}}
              (aux.webserver/request! {:method  :get
                                       :uri     (str "/plus?x=" 3 "&y=" 4)})))
    (flow "should sum the body x & y via post"
      (match? {:status 200
               :body {:total 7}}
              (aux.webserver/request! {:method  :post
                                       :uri     "/plus"
                                       :body    {:x 4
                                                 :y 3}})))))
