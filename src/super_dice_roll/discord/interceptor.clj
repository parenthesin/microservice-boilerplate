(ns super-dice-roll.discord.interceptor
  (:require [parenthesin.logs :as logs]
            [super-dice-roll.discord.security :as discord.security]))

(defn valid-request-interaction? [public-key timestamp body signature]
  (discord.security/verify-request public-key timestamp body signature))

(defn authentication-interceptor []
  {:name ::validate-request-interaction
   :enter (fn [ctx]
            (let [request (:request ctx)
                  {{:keys [config]} :components} request
                  app-public-key (get-in config [:config :discord :app-public-key])
                  x-signature-ed25519 (get-in ctx [:request :headers "x-signature-ed25519"])
                  x-signature-timestamp (get-in ctx [:request :headers "x-signature-timestamp"])
                  raw-body (slurp (:raw-body ctx))
                  is-valid-request? (valid-request-interaction? app-public-key
                                                                x-signature-timestamp
                                                                raw-body
                                                                x-signature-ed25519)]

              (logs/log :info {:header {:sig x-signature-ed25519
                                        :time x-signature-timestamp}
                               :public-key app-public-key
                               :body raw-body
                               :valid is-valid-request?})

              (if is-valid-request?
                ctx
                (assoc ctx :response {:headers {"Content-Type" "application/text"}
                                      :status 401
                                      :body "invalid request signature"}))))})
