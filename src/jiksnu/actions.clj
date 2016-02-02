(ns jiksnu.actions
  (:require [ciste.commands :refer [add-command!]]
            [ciste.core :refer [with-format with-serialization]]
            [ciste.filters :refer [filter-action]]
            [ciste.routes :refer [resolve-routes]]
            [clojure.core.incubator :refer [dissoc-in]]
            [jiksnu.channels :as ch]
            [jiksnu.predicates :as pred]
            [manifold.bus :as bus]
            [slingshot.slingshot :refer [throw+ try+]]
            [taoensso.timbre :as timbre]))

(defn get-model
  [model-name id]
  (let [model-ns (symbol (str "jiksnu.model." model-name))]
    (require model-ns)
    (let [fetcher (ns-resolve model-ns 'fetch-by-id)]
      (timbre/debugf "getting model %s(%s)" model-name id)
      (fetcher id))))

(defn get-page-ids
  [page-name & args]
  ;; (timbre/debugf "Getting page: %s" page-name)
  (let [request {:format :page-ids
                 :serialization :page-ids
                 :name page-name
                 :args args}]
    (or
     (try
       ((resolve-routes [@pred/*page-predicates*]
                        @pred/*page-matchers*) request)
       (catch Throwable ex
         ;; FIXME: Handle error
         ))
     (throw+ "page not found"))))

(defn get-page
  [page-name & args]
  (timbre/debugf "Getting page: %s" page-name)
  (let [request {:format :page
                 :serialization :page
                 :name page-name
                 :args args}]
    (or
     (try
       ((resolve-routes [@pred/*page-predicates*]
                        @pred/*page-matchers*) request)
       (catch Throwable ex
         ;; FIXME: Handle error
         ))
     (throw+ "page not found"))))

(defn get-sub-page-ids
  [item page-name & args]
  (timbre/debugf "Getting sub-page: %s(%s) => %s" (class item) (:_id item) page-name)
  (let [request {:format :page-ids
                 :serialization :page-ids
                 :name page-name
                 :item item
                 :args args}
        route-handler (resolve-routes [@pred/*sub-page-predicates*]
                                      @pred/*sub-page-matchers*)]
    (or (route-handler request)
        (throw+ {:action "error"
                 :page page-name
                 :item item
                 :args args
                 :message "sub page not found"}))))

(defn get-sub-page
  [item page-name & args]
  (timbre/debugf "Getting sub-page: %s(%s) => %s" (class item) (:_id item) page-name)
  (let [request {:format :page
                 :serialization :page
                 :name page-name
                 :item item
                 :args args}
        route-handler (resolve-routes [@pred/*sub-page-predicates*]
                                      @pred/*sub-page-matchers*)]
    (or (route-handler request)
        (throw+ {:action "error"
                 :page page-name
                 :item item
                 :args args
                 :message "sub page not found"}))))

(defn invoke-action
  [model-name action-name id & [options]]
  (try+
    (timbre/infof "Invoking Action. %s(%s) => %s" model-name id action-name)
   (let [action-ns (symbol (str "jiksnu.actions." model-name "-actions"))]
     (require action-ns)

     (if-let [action (ns-resolve action-ns (symbol action-name))]
       (let [body (with-serialization :command
                    (with-format :clj
                      (filter-action action id)))
             response {:message "action invoked"
                       :model model-name
                       :action action-name
                       :id id
                       :body body}]
         (bus/publish! ch/events ":actions:invoked" response)
         response)
       (do
         (timbre/warnf "could not find action for: %s(%s) => %s"
                    model-name id action-name)
         {:message (format "action not found: %s" action-name)
          :action "error"})))
   (catch RuntimeException ex
     (timbre/error "Actions error" ex)
     {:message (str ex)
      :action "error"})))

(defn confirm
  [action model id]
  (when-let [item (get-model model id)]
    {:item item
     :action action}))
