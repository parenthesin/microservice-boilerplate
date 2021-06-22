(ns migrations.core
  (:require [migratus.core :as migratus]
            [next.jdbc :as jdbc]
            [parenthesin.components.config :as components.config]))

(defn get-connection []
  (let [{:keys [username] :as db} (-> (components.config/read-config {}) :database)]
    (jdbc/get-connection (assoc db :user username))))

(def configuration
  {:store         :database
   :migration-dir "migrations/"})

(defn configuration-with-db []
  (assoc configuration :db {:connection (get-connection)}))

(defn init
  [config]
  (migratus/init config))

(defn migrate
  [config]
  (migratus/migrate config))

(defn up
  [config & args]
  (migratus/up config args))

(defn down
  [config & args]
  (migratus/down config args))

(defn create
  [config migration-name]
  (migratus/create config migration-name))

(defn rollback
  [config]
  (migratus/rollback config))

(defn pending-list
  [config]
  (migratus/pending-list config))

(defn migrate-until-just-before
  [config & args]
  (migratus/migrate-until-just-before config args))

(defn -main
  [& args]
  (let [arg (first args)]
    (cond
      (= arg "init") (init (configuration-with-db))
      (= arg "migrate") (migrate (configuration-with-db))
      (= arg "up") (up (configuration-with-db) (rest args))
      (= arg "down") (down (configuration-with-db) (rest args))
      (= arg "create") (create configuration (second args))
      (= arg "rollback") (rollback (configuration-with-db))
      (= arg "pending-list") (pending-list (configuration-with-db))
      (= arg "until-just-before") (migrate-until-just-before (configuration-with-db)
                                                             (rest args))
      :else
      (throw (Exception. (str "Command not found " arg))))))
