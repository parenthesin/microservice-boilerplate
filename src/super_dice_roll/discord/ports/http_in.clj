(ns super-dice-roll.discord.ports.http-in
  (:require [clojure.string :as string]
            [parenthesin.logs :as logs]
            [schema.core :as s]
            [super-dice-roll.discord.adapters :as adapters]
            [super-dice-roll.discord.controllers :as discord.controller]
            [super-dice-roll.discord.schemas.http-in :as discord.schemas.http-in]
            [super-dice-roll.schemas.types :as schemas.types]))

(s/defn temporary-roll-command->zoeira
  [{:keys [roll total results]}]
  (let [{:keys [nick username]} (get-in roll [:command :user])
        command (get-in roll [:command :command])]
    (str "Fala mano bronkx " (if (empty? nick) username nick) "\n"
         "já lhe estou lhe reconhecendo mermão, \n"
         "hummmmm... \"" command "\", *rolou dado pra caralho eim...* \n"
         (string/join "," results) " num **total de " total "**\n")))

(s/defn application-command-handler!
  [body :- discord.schemas.http-in/InteractionRequest
   components :- schemas.types/Components]
  (let [rolled (-> body
                   adapters/wire-in->model
                   (discord.controller/do-roll! components))]
    (if rolled
      {:status 200
       :body {:type 4
              :data {:content (temporary-roll-command->zoeira rolled)}}}
      {:status 200
       :body {:type 4
              :data {:content "Errou! Vish... digita /help ai trouxa!"}}})))

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
