(ns microservice-boilerplate.db
  (:require [parenthesin.components.database :as components.database]
            [microservice-boilerplate.schemas.types :as schemas.types]
            [microservice-boilerplate.schemas.db :as schemas.db]
            [honeysql.format :as sql.format]
            [honeysql.helpers :as sql.helpers]
            [schema.core :as s]))

(s/defn insert-wallet-transaction
  [transaction :- schemas.db/Wallet
   db :- schemas.types/DatabaseComponent]
  (components.database/execute
   db
   (-> (sql.helpers/insert-into :wallet)
       (sql.helpers/values [transaction])
       sql.format/format)))

(s/defn get-wallet-all-transactions
  [db :- schemas.types/DatabaseComponent]
  (components.database/execute
   db
   (-> (sql.helpers/select :id :btc_amount :usd_amount_at)
       (sql.helpers/from :wallet)
       sql.format/format)))
