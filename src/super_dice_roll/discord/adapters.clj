(ns super-dice-roll.discord.adapters
  (:require [schema.core :as s]
            [super-dice-roll.discord.schemas.http-in :as discord.schemas.http-in]
            [super-dice-roll.schemas.models :as schemas.models]))

(s/defn wire-in->model :- schemas.models/RollCommand
  [{{:keys [user nick]} :member
    {:keys [options]} :data} :- discord.schemas.http-in/InteractionRequest]
  (let [{:keys [id username]} user
        command (-> options first :value)]
    (println id username options)
    {:user {:id id
            :username username
            :nick (str nick)}
     :command command}))
