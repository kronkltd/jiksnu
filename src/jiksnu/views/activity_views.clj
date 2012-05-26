(ns jiksnu.views.activity-views
  (:use [ciste.config :only [config]]
        [ciste.debug :only [spy]]
        [ciste.views :only [defview]]
        ciste.sections.default
        jiksnu.actions.activity-actions)
  (:require [clj-tigase.core :as tigase]
            [clj-tigase.element :as element]
            [clj-tigase.packet :as packet]
            [jiksnu.abdera :as abdera]
            [jiksnu.model :as model]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.user :as model.user]
            [jiksnu.namespace :as namespace]
            [jiksnu.sections.activity-sections :as sections.activity]
            [jiksnu.session :as session]
            [jiksnu.xmpp.element :as xmpp.element]
            [plaza.rdf.core :as plaza]
            [plaza.rdf.vocabularies.foaf :as foaf]
            [ring.util.response :as response]))

(defview #'delete :html
  [request activity]
  (-> (response/redirect-after-post "/")
      (assoc :template false)))

(defview #'edit-page :html
  [request activity]
  {:body (edit-form activity)})

(defview #'post :html
  [request activity]
  (let [actor (session/current-user)
        url (or (-> request :params :redirect_to)
                "/" (uri actor))]
    (-> (response/redirect-after-post url)
        (assoc :template false))))

(defview #'remote-create :xmpp
  [request _]
  nil)

(defview #'show :clj
  [request activity]
  {:body activity})

(defview #'show :html
  [request activity]
  {:body (show-section activity)})

(defview #'show :n3
  [request activity]
  {:body (-> activity
             index-section
             plaza/model-add-triples
             plaza/defmodel
             (plaza/model-to-format :n3)
             with-out-str)
   :template :false})

(defview #'show :rdf
  [request activity]
  {:body (-> activity
             show-section
             plaza/model-add-triples
             plaza/defmodel
             (plaza/model-to-format :xml)
             with-out-str)
   :template :false})

(defview #'update :html
  [request activity]
  (let [actor (session/current-user)]
    (-> (response/redirect-after-post (uri actor))
        (assoc :template false))))
