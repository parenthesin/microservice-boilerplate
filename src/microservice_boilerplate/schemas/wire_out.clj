(ns microservice-boilerplate.schemas.wire-out
  (:require [schema.core :as s]))

(s/defschema KrakenResponse
  {:result {:XXBTZUSD {:c [s/Num]
                       s/Any s/Any}
            s/Any s/Any}
   s/Any s/Any})
