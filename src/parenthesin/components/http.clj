(ns parenthesin.components.http
  (:require [com.stuartsierra.component :as component]
            [java-http-clj.core :as http]
            [parenthesin.logs :as logs]
            [schema.core :as s]))

(s/defschema HttpRequestInput
  {:url s/Str
   :method (apply s/enum #{:get :head :post :put :delete :options :copy :move :patch})
   s/Any s/Any})

(s/defn request-fn
  [{:keys [url] :as req} :- HttpRequestInput]
  (-> req
      (assoc :uri url)
      http/send))

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
    (logs/log :info :http-out-message :method method :url url)
    (let [start-time (System/currentTimeMillis)
          {:keys [status] :as response} (request-fn request-input)
          end-time (System/currentTimeMillis)
          total-time (- start-time end-time)]
      (logs/log :info :http-out-message-response :response-time-millis total-time
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
    (get-in @responses
            [url]
            {:status 500
             :body "Response not set in mocks!"})))

(defn reset-responses! [added-responses {:keys [responses]}]
  (reset! responses added-responses))

(defn new-http-mock
  [mocked-responses]
  (map->HttpMock {:responses (atom mocked-responses)
                  :requests (atom [])}))
