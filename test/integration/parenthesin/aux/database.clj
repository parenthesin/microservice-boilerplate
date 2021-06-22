(ns integration.parenthesin.aux.database
  (:require [parenthesin.components.database :as db]
            [state-flow.api :as state-flow.api]
            [state-flow.core :as state-flow :refer [flow]]))

(defn execute!
  [commands]
  (flow "makes database execution"
    [database (state-flow.api/get-state :database)]
    (-> database
        (db/execute commands)
        state-flow.api/return)))
