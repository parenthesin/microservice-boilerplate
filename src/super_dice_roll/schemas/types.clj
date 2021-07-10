(ns super-dice-roll.schemas.types
  (:require [com.stuartsierra.component :as component]
            [parenthesin.components.database :as components.database]
            [parenthesin.components.http :as components.http]
            [schema.core :as s]))

(def HttpComponent (s/protocol components.http/HttpProvider))

(def DatabaseComponent (s/protocol components.database/DatabaseProvider))

(s/defschema Components
  {:config (s/protocol component/Lifecycle)
   :http HttpComponent
   :router (s/protocol component/Lifecycle)
   :database DatabaseComponent})
