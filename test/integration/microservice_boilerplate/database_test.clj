(ns integration.microservice-boilerplate.database-test
  (:require [clojure.test :as clojure.test]
            [integration.microservice-boilerplate.aux :as aux]
            [integration.microservice-boilerplate.aux.database :as aux.database]
            [schema.test :as schema.test]
            [state-flow.assertions.matcher-combinators :refer [match?]]
            [state-flow.core :as state-flow :refer [flow]]
            [state-flow.api :refer [defflow]]))

(clojure.test/use-fixtures :once schema.test/validate-schemas)

(defflow
  flow-integration-database-test
  {:init aux/start-system!
   :cleanup aux/stop-system!
   :fail-fast? true}
  (flow "creates a table, insert data and checks return in the database"
    (aux.database/execute! ["create table if not exists address (
                               id serial primary key,
                               name varchar(32),
                               email varchar(255))"])

    (aux.database/execute! ["insert into address(name,email)
                               values('Sam Campos de Milho','sammilhoso@email.com')"])

    (match? [#:address{:id 1
                       :name "Sam Campos de Milho"
                       :email "sammilhoso@email.com"}]
            (aux.database/execute! ["select * from address"]))))
