(ns super-dice-roll.discord.interceptor
  (:require [super-dice-roll.discord.security :as discord.security]))

(defn authentication-interceptor []
  {:name ::validate-request-interaction
   :enter (fn [ctx]
            (let [request (:request ctx)
                  {{:keys [config]} :components} request
                  app-public-key (get-in config [:config :discord :app-public-key])
                  x-signature-ed25519 (get-in ctx [:request :headers "x-signature-ed25519"])
                  x-signature-timestamp (get-in ctx [:request :headers "x-signature-timestamp"])
                  raw-body (slurp (:raw-body ctx))
                  is-valid-request? (discord.security/verify-request app-public-key
                                                                     x-signature-timestamp
                                                                     raw-body
                                                                     x-signature-ed25519)]
              (if is-valid-request?
                ctx
                (assoc ctx :response {:headers {"Content-Type" "application/text"}
                                      :status 401
                                      :body "invalid request signature"}))))})
