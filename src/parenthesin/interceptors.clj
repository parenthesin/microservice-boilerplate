(ns parenthesin.interceptors
  (:require [cheshire.core :as json]
            [clojure.string :as string]
            [clojure.walk :as walk]
            [exoscale.interceptor :as ix]
            [parenthesin.logs :as logs]
            [ring.util.codec :as codec]
            [schema-tools.coerce :as stc]
            [schema.coerce :as coerce]
            [schema.core :as s]))

(defn ^:private get-content-type [ctx]
  (or (get (:headers ctx) "content-type")
      (get (:headers ctx) "Content-Type")
      ""))

;; TODO unit tests
(def parse-body
  {:name :parse-request-body
   :enter
   (-> (fn [ctx] (assoc ctx
                        :body (-> ctx :body slurp (json/decode true))))
       (ix/when #(and (= (-> % :body type) java.io.ByteArrayInputStream)
                      (string/includes? (get-content-type %) "application/json"))))
   :leave
   (-> (fn [ctx] (-> ctx
                     (assoc :headers {"content-type" "application/json"})
                     (assoc :body (-> ctx :body (json/encode true)))))
       (ix/when #(and (or (= (-> % :body type) clojure.lang.PersistentArrayMap)
                          (= (-> % :body type) clojure.lang.PersistentVector))
                      (or (string/blank? (get-content-type %))
                          (string/includes? (get-content-type %)
                                            "application/json")))))
   :error (fn [ctx err]
            (logs/log :error :parse-request-body err ctx)
            {:status 500 :body (str err)})})

;; TODO unit tests
(def parse-query
  {:name :parse-request-query
   :enter (-> (fn [ctx] (assoc ctx
                               :query (->> ctx
                                           :query-string
                                           codec/form-decode
                                           walk/keywordize-keys)))
              (ix/when #(not (-> % :query-string nil?))))
   :error (fn [ctx err]
            (logs/log :error :parse-request-query err ctx)
            {:status 500 :body (str err)})})

(defn ^:private schema-coercer
  [schema matcher data]
  (let [parse (coerce/coercer! schema matcher)
        coerced (parse data)]
    (s/validate schema coerced)
    coerced))

;; TODO unit tests
(def coerce-schema
  {:name :coerce-request-schema
   :enter (fn [{:keys [params body query parameters] :as ctx}]
            (let [str-matcher stc/string-coercion-matcher
                  body-matcher stc/json-coercion-matcher
                  parsed-query (when-let [query-schema (:query parameters)]
                                 (schema-coercer query-schema str-matcher query))
                  parsed-path (when-let [path-schema (:path parameters)]
                                (schema-coercer path-schema str-matcher params))
                  parsed-body (when-let [body-schema (:body parameters)]
                                (schema-coercer body-schema body-matcher body))]
              (cond-> ctx
                (not-empty parsed-query) (assoc :query parsed-query)
                (not-empty parsed-path) (assoc :params parsed-path)
                (not-empty parsed-body) (assoc :body parsed-body))))
   :leave (fn [{:keys [response responses] :as ctx}]
            (if-let [schema (get responses (:status response))]
              (->> response
                   (schema-coercer (assoc schema :status s/Int)
                                   stc/json-coercion-matcher)
                   (merge ctx))
              ctx))
   :error (fn [ctx err]
            (logs/log :warn :coerce-request-schema err ctx)
            {:status 400
             :body (str (ex-data err))})})

(def request-logger
  {:name :request-logger
   :enter (fn [ctx]
            (logs/log :info
                      :request
                      (select-keys ctx [:request-method :uri :protocol]))
            ctx)})

(def base-interceptors [parse-body parse-query coerce-schema])
