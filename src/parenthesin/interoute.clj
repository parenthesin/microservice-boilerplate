(ns parenthesin.interoute
  (:require [exoscale.interceptor :as ix]
            [parenthesin.logs :as logs]
            [ruuter.core :as ruuter]))

(defn ^:private process-route-response
  [{:keys [handler handler-error interceptors parameters responses]}]
  (fn [req]
    (ix/execute req
                (concat [{:name :prepare-ctx
                          :enter (fn [ctx] (assoc ctx
                                                  :parameters parameters
                                                  :responses responses))
                          :leave (fn [ctx] (dissoc ctx
                                                   :exoscale.interceptor/queue
                                                   :exoscale.interceptor/stack
                                                   :responses))}]
                        (or (-> req :interceptors) [])
                        interceptors
                        [{:name :handler-fn
                          :error (or handler-error
                                     (-> req :handler-error)
                                     (fn [ctx err]
                                       (logs/log :error {:ctx ctx :error err})
                                       {:status 500 :body (str err)}))
                          :enter (-> handler
                                     (ix/in [])
                                     (ix/out [:response]))
                          :leave (fn [ctx]
                                   (-> (select-keys ctx [:exoscale.interceptor/queue
                                                         :exoscale.interceptor/stack
                                                         :responses])
                                       (merge (:response ctx))))}]))))

(defn ^:private routes->ruuter [routes]
  (map #(assoc % :response (process-route-response %)) routes))

;; TODO unit test
(defn routes->handler
  ([routes]
   (routes->handler routes {}))
  ([routes ctx]
   (fn [request]
     (ruuter/route (routes->ruuter routes)
                   (merge request ctx)))))
