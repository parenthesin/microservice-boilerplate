(ns super-dice-roll.discord.interceptor
  (:require [parenthesin.logs :as logs]
            [super-dice-roll.discord.security :as discord.security]))

(defn valid-request-interaction? [public-key timestamp body signature]
  (logs/log :info {:header {:sig signature :time timestamp} :public-key public-key :body body})
  (discord.security/verify-request public-key timestamp body signature))

(defn authentication-interceptor []
  {:name ::validate-request-interaction
   :enter (fn [ctx]
            (let [request (:request ctx)
                  {{:keys [config]} :components} request
                  app-public-key (get-in config [:config :discord :app-public-key])
                  x-signature-ed25519 (get-in ctx [:request :headers "x-signature-ed25519"])
                  x-signature-timestamp (get-in ctx [:request :headers "x-signature-timestamp"])]

              (if (valid-request-interaction? app-public-key
                                              x-signature-timestamp
                                              (slurp (:raw-body ctx))
                                              x-signature-ed25519)
                ctx
                (assoc ctx :response {:status 401
                                      :body "invalid request signature"}))))})
