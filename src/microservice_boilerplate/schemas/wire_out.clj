(ns microservice-boilerplate.schemas.wire-out
  (:require [schema.core :as s]))

(s/defschema CoinDeskResponse
  {:bpi {:USD {:rate s/Str
               s/Any s/Any}
         s/Any s/Any}
   s/Any s/Any})
