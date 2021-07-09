(ns integration.super-dice-roll.util
  (:require [cheshire.core :as json]
            [com.stuartsierra.component :as component]
            [integration.parenthesin.util.webserver :as util.webserver]
            [parenthesin.logs :as logs]
            [parenthesin.migrations :as migrations]
            [pg-embedded-clj.core :as pg-emb]
            [state-flow.api :as state-flow.api]
            [state-flow.core :as state-flow :refer [flow]]
            [super-dice-roll.discord.security :as discord.security]))

(defn signed-request!
  [{:keys [body] :as request}]
  (flow "makes http request"
    [signer (state-flow.api/get-state (comp :app-test-signer :discord :config :config))
     :let [timestamp (str (quot (System/currentTimeMillis) 1000))
           signature (->> (str timestamp (json/encode body))
                          .getBytes
                          (discord.security/sign signer)
                          discord.security/bytes->hex)]]
    (util.webserver/request! (-> request
                                 (assoc-in [:headers "x-signature-ed25519"] signature)
                                 (assoc-in [:headers "x-signature-timestamp"] timestamp)))))

(defn start-system!
  [system-start-fn]
  (fn []
    (logs/setup [["*" :info]] :auto)
    (pg-emb/init-pg)
    (migrations/migrate (migrations/configuration-with-db))
    (system-start-fn)))

(defn stop-system!
  [system]
  (component/stop-system system)
  (pg-emb/halt-pg!))
