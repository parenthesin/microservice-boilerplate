(ns microservice-boilerplate.components.database
  (:require [next.jdbc :as jdbc]))

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
create table address (
  id serial primary key,
  name varchar(32),
  email varchar(255)
)"])

  (jdbc/execute! ds ["
insert into address(name,email)
  values('Sean Corfield','sean@corfield.org')"])

  (jdbc/execute! ds ["select * from address"])

  (pg-emb/halt-pg!))
