(ns microservice-boilerplate.schemas.types
  (:require [clojure.test.check.generators :as generators]
            [com.stuartsierra.component :as component]
            [parenthesin.components.database :as components.database]
            [parenthesin.components.http :as components.http]
            [schema.core :as s]))

(def PositiveNumber
  (s/constrained s/Num pos? 'PositiveNumber))

(def PositiveNumberGenerator
  (generators/fmap bigdec (generators/double* {:infinite? false :NaN? false :min 0.0001})))

(def NegativeNumber
  (s/constrained s/Num neg? 'NegativeNumber))

(def NegativeNumberGenerator
  (generators/fmap bigdec (generators/double* {:infinite? false :NaN? false :max -0.0001})))

(def NumberGenerator
  (generators/fmap bigdec (generators/double* {:infinite? false :NaN? false})))

(def TypesLeafGenerators
  {PositiveNumber PositiveNumberGenerator
   NegativeNumber NegativeNumberGenerator
   s/Num NumberGenerator})

(def HttpComponent (s/protocol components.http/HttpProvider))

(def DatabaseComponent (s/protocol components.database/DatabaseProvider))

(s/validate {:fn (s/pred fn?)} {:fn #(%)})

(def Route
  {:path s/Str
   :method s/Keyword
   :handler (s/pred fn?)
   (s/optional-key :summary) s/Str
   (s/optional-key :parameters) s/Any
   (s/optional-key :responses) s/Any})

(def Interceptor
  {:name s/Keyword
   (s/optional-key :enter) (s/pred fn?)
   (s/optional-key :leave) (s/pred fn?)
   (s/optional-key :error) (s/pred fn?)})

(s/defschema Components
  {:config (s/protocol component/Lifecycle)
   :http HttpComponent
   :database DatabaseComponent
   :routes [Route]
   :interceptors [Interceptor]})
