(ns super-dice-roll.discord.ports.http-in
  (:require [parenthesin.logs :as logs]
            [schema.core :as s]
            [super-dice-roll.discord.adapters :as adapters]
            [super-dice-roll.discord.schemas.http-in :as discord.schemas.http-in]
            [super-dice-roll.schemas.types :as schemas.types]))

(s/defn temporary-roll-command->zoeira
  [{:keys [user command]}]
  (str "Fala mano bronkx " (if (empty? (:nick user)) (:username user) (:nick user)) "\n"
       "já lhe estou lhe reconhecendo mermão, \n"
       "porém *ainda* não sei fazer nada ainda **parsa!** \n"
       "Mas assim parece bem maneiro esse tal de \"" command "\""))

(s/defn application-command-handler!
  [body :- discord.schemas.http-in/InteractionRequest
   _components :- schemas.types/Components]
  (let [roll-command (adapters/wire-in->model body)]
    {:status 200
     :body {:type 4
            :data {:content (temporary-roll-command->zoeira roll-command)}}}))

(defn process-interaction!
  [{{{:keys [type] :as body} :body
     headers :header} :parameters
    components :components}]
  (logs/log :info {:header headers :body body})
  (case type
    1 {:status 200
       :body {:type 1}}
    2 (application-command-handler! body components)
    {:status 200
     :body {:type 4
            :data {:content (str "Unknown command, try one of the following **slash commands**: \n"
                                 "`/roll`, `/history` or `/help`.")}}}))
