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
            :data {:tts false
                   :content "Aeeeeeeee caralho! Fala brother beleza? Eu não sei fazer nada ainda parsa!"
                   :embeds []
                   :allowed_mentions {:parse []}}}}))
