(ns microservice-boilerplate.components.http
  (:require [clj-http.client :as http]
            [clj-http.util :as http-util]
            [clojure.tools.logging :as log]
            [com.stuartsierra.component :as component]
            [schema.core :as s]))

(s/defschema HttpRequestInput
  {:url s/Str
   :method (apply s/enum #{:get :head :post :put :delete :options :copy :move :patch})
   s/Any s/Any})

(s/defn request-fn
  [{:keys [url] :as req} :- HttpRequestInput
   & [respond raise]]
  (http/check-url! url)
  (if (http-util/opt req :async)
    (if (some nil? [respond raise])
      (throw (IllegalArgumentException.
              "If :async? is true, you must pass respond and raise"))
      (http/request (dissoc req :respond :raise) respond raise))
    (http/request req)))

(defprotocol HttpProvider
  (request
    [self request-input]))

(defrecord Http [_]
  component/Lifecycle
  (start [this] this)
  (stop  [this] this)

  HttpProvider
  (request
    [_self {:keys [method url] :as request-input}]
    (log/info :http-out-message :method method :url url)
    (let [start-time (System/currentTimeMillis)
          {:keys [status] :as response} (request-fn request-input)
          end-time (System/currentTimeMillis)
          total-time (- start-time end-time)]
      (log/info :http-out-message-response :response-time-millis total-time
                :status status)
      response)))

(defn new-http [] (map->Http {}))

(defrecord HttpMock [responses requests]
  component/Lifecycle
  (start [this] this)
  (stop  [this] this)

  HttpProvider
  (request
    [_self {:keys [url] :as req}]
    (swap! requests merge
           (assoc req :instant (System/currentTimeMillis)))
    (get-in responses
            [url]
            {:status 500
             :body "Response not set in mocks!"})))

(defn new-http-mock
  [mocked-responses]
  (map->HttpMock {:responses mocked-responses
                  :requests (atom [])}))
