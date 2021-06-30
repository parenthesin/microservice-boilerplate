(ns microservice-boilerplate.schemas.db
  (:require [schema.core :as s]))

(def wallet {:wallet/id s/Uuid
             :wallet/btc_amount s/Num
             :wallet/usd_amount_at s/Num
             :wallet/created_at s/Inst})

(s/defschema WalletTransaction
  (select-keys wallet [:wallet/id
                       :wallet/btc_amount
                       :wallet/usd_amount_at]))

(s/defschema WalletEntry
  (select-keys wallet [:wallet/id
                       :wallet/btc_amount
                       :wallet/usd_amount_at
                       :wallet/created_at]))
