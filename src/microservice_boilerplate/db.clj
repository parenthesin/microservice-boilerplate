(ns microservice-boilerplate.db
  (:require [parenthesin.components.database :as components.database]
            [microservice-boilerplate.schemas.types :as schemas.types]
            [honeysql.format :as sql.format]
            [honeysql.helpers :as sql.helpers]
            [schema.core :as s]))

;; TODO - Schema db wallet transaction
(s/defn insert-wallet-transaction
  [transaction :- s/Any
   db :- schemas.types/DatabaseComponent]
  (->>
   (-> (sql.helpers/insert-into :wallet)
       (sql.helpers/values [transaction])
       sql.format/format)
   (components.database/execute db)))
