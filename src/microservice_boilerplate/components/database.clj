(ns microservice-boilerplate.components.database
  (:require [next.jdbc :as jdbc]
            [com.stuartsierra.component :as component]))

(defprotocol DatabaseProvider
  (execute [self command]
    "Low-level API to execute a command in the database"))

;; TODO - allow keeping a connection pool instead of opening new connection each time an operation is done
;; https://cljdoc.org/d/seancorfield/next.jdbc/1.0.13/doc/getting-started#connection-pooling
(defrecord Database [connection-options]

  component/Lifecycle
  (start [this]
    (->> connection-options
         (jdbc/get-datasource)
         (assoc this :data-source)))

  (stop [this] this)

  DatabaseProvider
  (execute [this commands]
    (jdbc/execute! (:data-source this) commands)))

(defn new-database
  [opts]
  (map->Database {:connection-options opts}))

(comment

  (def db (component/start (new-database {:dbtype "sqlite"
                                          :dbname "sqlite-db"})))
  (execute db ["
create table if not exists address (
  id serial primary key,
  name varchar(32),
  email varchar(255)
)"])

  (execute db ["
insert into address(name,email)
  values('Sean Corfield','sean@corfield.org')"])

  (execute db ["select * from address"]))

(comment
  (require '[pg-embedded-clj.core :as pg-emb])

  (pg-emb/init-pg)


  (def db {:dbtype "postgres"
           :dbname "postgres"
           :user "postgres"
           :password "postgres"
           :host "localhost"
           :port "5432"})

  (def ds (jdbc/get-datasource db))

  (jdbc/execute! ds ["
create table if not exists address (
  id serial primary key,
  name varchar(32),
  email varchar(255)
)"])

  (jdbc/execute! ds ["
insert into address(name,email)
  values('Sean Corfield','sean@corfield.org')"])

  (jdbc/execute! ds ["select * from address"])

  (pg-emb/halt-pg!))
