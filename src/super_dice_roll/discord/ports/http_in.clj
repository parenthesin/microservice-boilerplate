(ns super-dice-roll.discord.ports.http-in
  (:require [parenthesin.logs :as logs]))

(defn process-interaction!
  [{{{:keys [type] :as body} :body
     headers :header} :parameters
    _components :components}]
  (logs/log :info {:header headers :body body})
  (case type
    1 {:status 200
       :body {:type 1}}
    {:status 200
     :body {:type 4
            :tts false
            :content "Congrats on sending your command!"
            :embeds []
            :allowed_mentions {:parse []}}}))
