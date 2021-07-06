(ns super-dice-roll.discord.interceptor
  (:require [parenthesin.logs :as logs]))

(defn valid-request-interaction? [public-key signature timestamp body]
  (logs/log :info {:header {:sig signature :time timestamp} :public-key public-key :body body})
  true)

(defn authentication-interceptor []
  {:name ::validate-request-interaction
   :enter (fn [ctx]
            (let [request (:request ctx)
                  {{:keys [config]} :components} request
                  app-public-key (get-in config [:config :discord :app-public-key])
                  x-signature-ed25519 (get-in ctx [:request :headers "x-signature-ed25519"])
                  x-signature-timestamp (get-in ctx [:request :headers "x-signature-timestamp"])]

              (if (valid-request-interaction? app-public-key x-signature-ed25519 x-signature-timestamp (:body request))
                ctx
                (assoc ctx :response {:status 401 :body "invalid request signature"}))))})
