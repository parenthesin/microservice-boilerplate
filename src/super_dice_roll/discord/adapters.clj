(ns super-dice-roll.discord.adapters
  (:require [clojure.string :as string]
            [schema.core :as s]
            [super-dice-roll.discord.schemas.http-in :as discord.schemas.http-in]
            [super-dice-roll.messages :as messages]
            [super-dice-roll.schemas.models :as schemas.models]))

(s/defn wire-in->model :- schemas.models/RollCommand
  [{{:keys [user nick]} :member
    {:keys [options]} :data} :- discord.schemas.http-in/InteractionRequest]
  (let [{:keys [id username]} user
        command (-> options first :value)]
    {:user {:id id
            :username username
            :nick (str nick)}
     :command command}))

(s/defn rolled->message :- s/Str
  [{:keys [roll total results]}]
  (let [{:keys [nick username]} (get-in roll [:command :user])
        command (get-in roll [:command :command])]
    (str "*" (if (empty? nick) username nick) " rolled " command "*\n"
         "`[" (string/join "," results) "]"
         (when-not (zero? (:modifier roll)) (str " + " (:modifier roll))) "`\n"
         "**total: " total "**\n")))

(s/defn roll-command->error-message :- s/Str
  [{:keys [user command]} :- schemas.models/RollCommand]
  (let [{:keys [nick username]} user]
    (str (if (empty? nick) username nick) " the command *" command "* is invalid\n"
         messages/help-roll)))
