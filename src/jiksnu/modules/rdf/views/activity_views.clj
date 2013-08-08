(ns jiksnu.modules.rdf.views.activity-views
  (:use [ciste.views :only [defview]]
        [ciste.sections.default :only [index-section show-section]]
        [jiksnu.actions.activity-actions :only [show]])
  (:require [plaza.rdf.core :as plaza]))

(defview #'show :n3
  [request activity]
  {:body (-> activity
             index-section
             plaza/model-add-triples
             plaza/defmodel
             (plaza/model->format :n3)
             with-out-str)
   :template :false})

(defview #'show :rdf
  [request activity]
  {:body (-> activity
             show-section
             plaza/model-add-triples
             plaza/defmodel
             (plaza/model->format :xml)
             with-out-str)
   :template :false})
