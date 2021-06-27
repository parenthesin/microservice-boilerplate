(ns microservice-boilerplate.schemas.db
  (:require [schema.core :as s]))

(s/defschema Wallet
  {:wallet/id s/Uuid
   :wallet/btc_amount s/Num
   :wallet/usd_amount_at s/Num
   (s/optional-key :wallet/created_at) s/Inst})
