(ns super-dice-roll.discord.interceptor
  (:require [parenthesin.logs :as logs]
            [super-dice-roll.discord.security :as discord.security]))

(defn try-verify-request
  [public-key signature timestamp body]
  (try
    (discord.security/verify-request public-key timestamp body signature)
    (catch Exception e
      (logs/log :warn :invalid-signed-request
                {:public-key public-key
                 :timestamp timestamp
                 :signature signature
                 :error (.getMessage e)})
      false)))

(defn authentication-interceptor []
  {:name ::validate-request-interaction
   :enter (fn [ctx]
            (let [request (:request ctx)
                  {{:keys [config]} :components} request
                  public-key (get-in config [:config :discord :app-public-key])
                  signature (get-in ctx [:request :headers "x-signature-ed25519"])
                  timestamp (get-in ctx [:request :headers "x-signature-timestamp"])
                  raw-body (slurp (:raw-body ctx))]
              (if (try-verify-request public-key signature timestamp raw-body)
                ctx
                (assoc ctx :response {:headers {"Content-Type" "application/text"}
                                      :status 401
                                      :body "invalid request signature"}))))})
