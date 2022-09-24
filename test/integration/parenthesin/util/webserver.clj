(ns integration.parenthesin.util.webserver
  (:require [cheshire.core :as json]
            [clojure.string :as string]
            [ring.mock.request :as mock]
            [state-flow.api :as state-flow.api]
            [state-flow.core :as state-flow :refer [flow]]))

(defn- do-request [route-handlers verb route body headers]
  (let [request (-> (mock/request verb route)
                    (merge {:headers headers}))
        request-body (if body
                       (mock/json-body request body)
                       request)]
    (route-handlers request-body)))

(defn- parsed-response
  [{:keys [headers body] :as request}]
  (if (string/includes? (get headers "content-type" "") "application/json")
    (assoc request :body (json/decode body true))
    request))

(defn request!
  [{:keys [method uri body headers]}]
  (flow "makes http request"
    [route-handlers (state-flow.api/get-state (comp :route-handlers :webserver))]
    (-> route-handlers
        (do-request method uri body headers)
        parsed-response
        state-flow.api/return)))
