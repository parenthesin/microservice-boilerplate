(ns integration.parenthesin.util.webserver
  (:require [cheshire.core :as json]
            [clojure.string :as string]
            [io.pedestal.test :as pt]
            [state-flow.api :as state-flow.api]
            [state-flow.core :as state-flow :refer [flow]]))

(defn- do-request [service-fn verb route body headers]
  (let [headers-with-default (merge {"Content-Type" "application/json"} headers)
        encoded-body (json/encode body)]
    (pt/response-for service-fn verb route :headers headers-with-default :body encoded-body)))

(defn- parsed-response
  [{:keys [headers body] :as request}]
  (if (string/includes? (get headers "Content-Type") "application/json")
    (assoc request :body (json/decode body true))
    request))

(defn request!
  [{:keys [method uri body headers]}]
  (flow "makes http request"
    [service-fn (state-flow.api/get-state (comp :io.pedestal.http/service-fn :webserver :webserver))]
    (-> service-fn
        (do-request method uri body headers)
        parsed-response
        state-flow.api/return)))
