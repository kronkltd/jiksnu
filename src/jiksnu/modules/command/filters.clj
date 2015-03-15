(ns jiksnu.modules.command.filters
  (:require [ciste.filters :refer [deffilter]]
            [clojure.tools.logging :as log]
            [jiksnu.actions :as actions]
            ;; [jiksnu.modules.http.actions :as http.actions]
            [slingshot.slingshot :refer [throw+]]))

;; (deffilter #'http.actions/connect :command
;;   [action request]
;;   (action (:channel request)))

(deffilter #'actions/get-model :command
  [action request]
  (let [[model-name id] (:args request)]
    (or (action model-name id)
        (throw+ "Model not found"))))

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

