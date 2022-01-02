(ns integration.parenthesin.util.http
  (:require [parenthesin.components.http :as components.http]
            [state-flow.api :as state-flow.api]
            [state-flow.core :as state-flow :refer [flow]]))

(defn set-http-out-responses!
  [responses]
  (flow "set http-out mock responses"
    [http (state-flow.api/get-state (comp :http :webserver))]
    (-> responses
        (components.http/reset-responses! http)
        state-flow.api/return)))

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn http-out-requests []
  (flow "get http-out mock requests"
    (state-flow.api/get-state (comp deref :requests :http :webserver))))
