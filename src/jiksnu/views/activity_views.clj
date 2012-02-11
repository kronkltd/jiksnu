(ns jiksnu.views.activity-views
  (:use (ciste [config :only [config]]
               core
               [debug :only [spy]]
               sections
               [views :only [defview]])
        ciste.sections.default
        jiksnu.actions.activity-actions)
  (:require (clj-tigase [core :as tigase]
                        [element :as element]
                        [packet :as packet])
            (jiksnu [abdera :as abdera]
                    [model :as model]
                    [namespace :as namespace]
                    [session :as session]
                    [view :as view])
            (jiksnu.helpers [activity-helpers :as helpers.activity])
            (jiksnu.model [activity :as model.activity]
                          [user :as model.user])
            (jiksnu.sections [activity-sections :as sections.activity])
            (jiksnu.xmpp [element :as xmpp.element])
            (plaza.rdf [core :as plaza])
            (plaza.rdf.vocabularies [foaf :as foaf])
            (ring.util [response :as response])))

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
