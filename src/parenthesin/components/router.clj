(ns parenthesin.components.router
  (:require [com.stuartsierra.component :as component]
            [muuntaja.core :as m]
            [reitit.coercion.schema :as reitit.schema]
            [reitit.dev.pretty :as pretty]
            [reitit.http :as http]
            [reitit.http.coercion :as coercion]
            [reitit.http.interceptors.exception :as exception]
            [reitit.http.interceptors.multipart :as multipart]
            [reitit.http.interceptors.muuntaja :as muuntaja]
            [reitit.http.interceptors.parameters :as parameters]
            [reitit.pedestal :as pedestal]
            [reitit.ring :as ring]
            [reitit.swagger :as swagger]
            [reitit.swagger-ui :as swagger-ui]))

(def router-settings
  {;:reitit.interceptor/transform dev/print-context-diffs ;; pretty context diffs
     ;;:validate spec/validate ;; enable spec validation for route data
     ;;:reitit.spec/wrap spell/closed ;; strict top-level validation
   :exception pretty/exception
   :data {:coercion reitit.schema/coercion
          :muuntaja m/instance
          :interceptors [;; swagger feature
                         swagger/swagger-feature
                             ;; query-params & form-params
                         (parameters/parameters-interceptor)
                             ;; content-negotiation
                         (muuntaja/format-negotiate-interceptor)
                             ;; encoding response body
                         (muuntaja/format-response-interceptor)
                             ;; exception handling
                         (exception/exception-interceptor)
                             ;; decoding request body
                         (muuntaja/format-request-interceptor)
                             ;; coercing response bodys
                         (coercion/coerce-response-interceptor)
                             ;; coercing request parameters
                         (coercion/coerce-request-interceptor)
                             ;; multipart
                         (multipart/multipart-interceptor)]}})

(defn router [routes]
  (pedestal/routing-interceptor
   (http/router routes router-settings)
    ;; optional default ring handler (if no routes have matched)
   (ring/routes
    (swagger-ui/create-swagger-ui-handler
     {:path "/"
      :config {:validatorUrl nil
               :operationsSorter "alpha"}})
    (ring/create-resource-handler)
    (ring/create-default-handler))))

(defrecord Router [router]
  component/Lifecycle
  (start [this] this)
  (stop  [this] this))

(defn new-router
  [routes]
  (map->Router {:router (router routes)}))
