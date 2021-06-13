(ns microservice-boilerplate.routes
  (:require [reitit.swagger :as swagger]
            [schema.core :as s]))

(def routes
  [["/swagger.json"
    {:get {:no-doc true
           :swagger {:info {:title "my-api"
                            :description "with pedestal & reitit-http"}}
           :handler (swagger/create-swagger-handler)}}]

   ["/math"
    {:swagger {:tags ["math"]}}

    ["/plus"
     {:get {:summary "plus with spec query parameters"
            :parameters {:query {:x s/Int, :y s/Int}}
            :responses {200 {:body {:total s/Int}}}
            :handler (fn [{{{:keys [x y]} :query} :parameters}]
                       {:status 200
                        :body {:total (+ x y)}})}
      :post {:summary "plus with spec body parameters"
             :parameters {:body {:x s/Int, :y s/Int}}
             :responses {200 {:body {:total s/Int}}}
             :handler (fn [{{{:keys [x y]} :body} :parameters}]
                        {:status 200
                         :body {:total (+ x y)}})}}]]])
