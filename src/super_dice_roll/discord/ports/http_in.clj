(ns super-dice-roll.discord.ports.http-in
  (:require [parenthesin.logs :as logs]
            [schema.core :as s]
            [super-dice-roll.discord.adapters :as adapters]
            [super-dice-roll.discord.controllers :as discord.controller]
            [super-dice-roll.discord.schemas.http-in :as discord.schemas.http-in]
            [super-dice-roll.messages :as messages]
            [super-dice-roll.schemas.types :as schemas.types]))

(s/defn application-command-handler!
  [body :- discord.schemas.http-in/InteractionRequest
   components :- schemas.types/Components]
  (let [slash-cmd (get-in body [:data :name])
        roll-cmd (adapters/wire-in->model body)
        content (case slash-cmd
                  "roll" (if-let [rolled (discord.controller/do-roll! roll-cmd components)]
                           (adapters/rolled->message rolled)
                           (adapters/roll-command->error-message roll-cmd))
                  "history" "Command not available."
                  "help" (str messages/help-header "\n"
                              messages/help-roll "\n"
                              messages/help-history))]
    {:status 200
     :body {:type 4
            :data {:content content}}}))

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
            :data {:content (str "Unknown command. " messages/help-header)}}}))
