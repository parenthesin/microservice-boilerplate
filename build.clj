(ns build
  (:refer-clojure :exclude [test])
  (:require [clojure.tools.build.api :as b]))

(def default-lib 'com.github.parenthesin/microservice-boilerplate)
(def default-main 'microservice-boilerplate.server)
(def default-version "0.0.1-SNAPSHOT")
(def class-dir "target/classes")

(defn- uber-opts [{:keys [lib main uber-file version] :as opts}]
  (let [actual-lib (or lib default-lib)
        actual-main (or main default-main)
        actual-version (or version default-version)
        actual-uber-file (or uber-file (format "target/%s-%s.jar"
                                               actual-lib
                                               actual-version))]
    (assoc opts
           :lib actual-lib
           :main actual-main
           :uber-file actual-uber-file
           :basis (b/create-basis {})
           :class-dir class-dir
           :src-dirs ["src"]
           :ns-compile [actual-main])))

(defn uberjar "Build the Uberjar." [opts]
  (b/delete {:path "target"})
  (let [{:keys [main uber-file] :as opts} (uber-opts opts)]
    (println "\nCopying source" class-dir)
    (b/copy-dir {:src-dirs ["resources" "src"] :target-dir class-dir})
    (println (str "\nCompiling " main))
    (b/compile-clj opts)
    (println "\nBuilding JAR on" uber-file)
    (b/uber opts))
  opts)
