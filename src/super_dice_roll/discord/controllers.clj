(ns super-dice-roll.discord.controllers 
  (:require [schema.core :as s]
            [super-dice-roll.controllers :as base.controller]
            [super-dice-roll.logics :as base.logics]
            [super-dice-roll.schemas.models :as schemas.models]
            [super-dice-roll.schemas.types :as schemas.types]))

(defn- instant-now [] (java.util.Date/from (java.time.Instant/now)))

(s/defn do-roll! :- (s/maybe schemas.models/Rolled)
  [roll-command :- schemas.models/RollCommand
   {:keys [_database]} :- schemas.types/Components]
  (let [_now (instant-now)
        roll (base.logics/roll-command->roll roll-command)]
    (when (base.logics/valid-roll? roll)
      (base.controller/roll->rolled roll))))
