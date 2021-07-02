(ns integration.parenthesin.database-test
  (:require [clojure.test :as clojure.test]
            [integration.parenthesin.util :as util]
            [integration.parenthesin.util.database :as util.database]
            [schema.test :as schema.test]
            [state-flow.api :refer [defflow]]
            [state-flow.assertions.matcher-combinators :refer [match?]]
            [state-flow.core :as state-flow :refer [flow]]))

(clojure.test/use-fixtures :once schema.test/validate-schemas)

(defflow
  flow-integration-database-test
  {:init util/start-system!
   :cleanup util/stop-system!
   :fail-fast? true}
  (flow "creates a table, insert data and checks return in the database"
    (util.database/execute! ["create table if not exists address (
                               id serial primary key,
                               name varchar(32),
                               email varchar(255))"])

    (util.database/execute! ["insert into address(name,email)
                               values('Sam Campos de Milho','sammilhoso@email.com')"])

    (match? [#:address{:id 1
                       :name "Sam Campos de Milho"
                       :email "sammilhoso@email.com"}]
            (util.database/execute! ["select * from address"]))))
