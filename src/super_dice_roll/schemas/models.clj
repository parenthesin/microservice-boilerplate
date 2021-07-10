(ns super-dice-roll.schemas.models
  (:require [schema.core :as s]))

(s/defschema User
  {:id s/Str
   :username s/Str
   :nick s/Str})

(s/defschema RollCommand
  {:user User
   :command s/Str})

(s/defschema Roll
  {:command RollCommand
   :times s/Int
   :dice s/Int
   :modifier s/Int})
