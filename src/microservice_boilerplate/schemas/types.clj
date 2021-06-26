(ns microservice-boilerplate.schemas.types
  (:require [com.stuartsierra.component :as component]
            [parenthesin.components.database :as components.database]
            [parenthesin.components.http :as components.http]
            [schema.core :as s]))

(def PositiveNumber (s/constrained s/Num pos? 'PositiveNumber))

(def NegativeNumber (s/constrained s/Num neg? 'NegativeNumber))

(def HttpComponent (s/protocol components.http/HttpProvider))

(def DatabaseComponent (s/protocol components.database/DatabaseProvider))

(s/defschema Components
  {:config (s/protocol component/Lifecycle)
   :http HttpComponent
   :router (s/protocol component/Lifecycle)
   :database DatabaseComponent
   :webserver (s/protocol component/Lifecycle)})
