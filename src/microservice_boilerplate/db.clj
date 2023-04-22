(ns microservice-boilerplate.db
  (:require [honey.sql :as sql]
            [honey.sql.helpers :as sql.helpers]
            [microservice-boilerplate.schemas.db :as schemas.db]
            [microservice-boilerplate.schemas.types :as schemas.types]
            [parenthesin.components.db.jdbc-hikari :as components.database]
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
