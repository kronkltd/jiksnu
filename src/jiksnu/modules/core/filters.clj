(ns jiksnu.filters
  (:use [ciste.filters :only [deffilter]]
        [clojure.core.incubator :only [-?>]]
        [slingshot.slingshot :only [throw+]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.actions :as actions]))

(defn parse-page
  [request]
  {:page (or (-?> request :params :page Integer/parseInt) 1)})

(defn parse-sorting
  [request]
  (let [order-by (:orderBy (:params request))
        direction (if (= "desc" (:direction (:params request))) -1 1)]
    (when (and order-by direction)
      {:sort-clause {(keyword order-by) direction}})))


(deffilter #'actions/confirm :http
  [action request]
  (let [params (:params request)]
    (action (:action params)
            (:model params)
            (:id params))))

(deffilter #'actions/connect :command
  [action request]
  (action (:channel request)))


(deffilter #'actions/get-model :command
  [action request]
  (let [[model-name id] (:args request)]
    (or (action model-name id)
        (throw+ "Model not found")
        #_{:action "error"
         :message "Model not found"})))



(deffilter #'actions/get-page :command
  [action request]
  (apply action (:args request)))

(deffilter #'actions/get-sub-page :command
  [action request]
  (let [[model-name id page-name] (:args request)]
    (if-let [item (actions/get-model model-name id)]
      (action item page-name))))

(deffilter #'actions/invoke-action :command
  [action request]
  (apply action (:args request)))



