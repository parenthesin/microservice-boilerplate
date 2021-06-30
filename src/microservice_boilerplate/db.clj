(ns microservice-boilerplate.db
  (:require [parenthesin.components.database :as components.database]
            [microservice-boilerplate.schemas.types :as schemas.types]
            [microservice-boilerplate.schemas.db :as schemas.db]
            [honey.sql :as sql]
            [honey.sql.helpers :as sql.helpers]
            [schema.core :as s]))

(s/defn insert-wallet-transaction
  [transaction :- schemas.db/WalletTransaction
   db :- schemas.types/DatabaseComponent]
   (->> (-> (sql.helpers/insert-into :wallet)
            (sql.helpers/values [transaction])
            (sql.helpers/returning :*)
            sql/format)
        (components.database/execute db)
        first))

(s/defn get-wallet-all-transactions :- [schemas.db/WalletEntry]
  [db :- schemas.types/DatabaseComponent]
  (components.database/execute
   db
   (-> (sql.helpers/select :id :btc_amount :usd_amount_at :created_at)
       (sql.helpers/from :wallet)
       sql/format)))

(s/defn get-wallet-total :- s/Num
  [db :- schemas.types/DatabaseComponent]
  (->> (-> (sql.helpers/select :%sum.btc_amount)
           (sql.helpers/from :wallet)
           sql/format)
       (components.database/execute db)
       first
       :sum))
