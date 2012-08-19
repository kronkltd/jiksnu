(ns jiksnu.views.activity-views
  (:use [ciste.config :only [config]]
        [ciste.views :only [defview]]
        ciste.sections.default
        jiksnu.actions.activity-actions)
  (:require [clj-tigase.core :as tigase]
            [clj-tigase.element :as element]
            [clj-tigase.packet :as packet]
            [clojure.tools.logging :as log]
            [jiksnu.abdera :as abdera]
            [jiksnu.model :as model]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.user :as model.user]
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

(defview #'delete :model
  [request activity]
  {:body (show-section activity)})




(defview #'edit-page :html
  [request activity]
  {:body (edit-form activity)})

(defview #'oembed :json
  [request oembed-map]
  {:status 200
   :body oembed-map}
  )

(defview #'oembed :xml
  [request m]
  {:status 200
   :body
   [:oembed
    [:version (:version m)]
    [:type (:type m)]
    [:provider_name (:provider_name m)]
    [:provider_url (:provider_url m)]
    [:title (:title m)]
    [:author_name (:author_name m)]
    [:author_url (:author_url m)]
    [:url (:url m)]
    [:html (:html m)]
    ]
   
   }
  )

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

(defview #'show :viewmodel
  [request activity]
  {:body {:activities (doall (index-section [activity]))
          :target (:_id activity)
          :model "activity"
          :displayMode "single"
          :showPostForm false
          :title (:title activity)}})

(defview #'update :html
  [request activity]
  (let [actor (session/current-user)]
    (-> (response/redirect-after-post (uri actor))
        (assoc :template false))))
