(ns jiksnu.views
  (:use [ciste.core :only [serialize-as with-format]]
        [ciste.config :only [config]]
        [ciste.formats :only [format-as]]
        [ciste.views :only [defview]]
        [ciste.sections :only [defsection]]
        [ciste.sections.default :only [full-uri title link-to
                                       index-block index-section uri
                                       delete-button index-line edit-button]]
        [jiksnu.session :only [current-user]])
  (:require [clj-tigase.core :as tigase]
            [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            [hiccup.core :as h]
            [jiksnu.abdera :as abdera]
            [jiksnu.actions :as actions]
            [jiksnu.model :as model]
            [jiksnu.namespace :as ns]
            jiksnu.sections
            [plaza.rdf.core :as rdf]
            [plaza.rdf.vocabularies.foaf :as foaf]))

(defn command-not-found
  []
  "Command not found")




(defmethod serialize-as :http
  [serialization response-map]
  (let [content-type (or (-> response-map :headers (get "Content-Type"))
                         "text/html; charset=utf-8")]
    (-> (merge {:status 200} response-map)
        (assoc-in  [:headers "Content-Type"] content-type))))

(defmethod serialize-as :xmpp
  [serialization response]
  (when response
    (tigase/make-packet response)))

(defmethod serialize-as :command
  [serialization response]
  response)

(defmethod serialize-as :page
  [serialization response]
  (json/read-json (:body response)))

(defview #'actions/invoke-action :json
  [request data]
  {:body data})

(defview #'actions/connect :json
  [request response]
  {:body {:action "connect"
          :connection-id response}})

(defview #'actions/get-model :json
  [request response]
  {:body {:action "model-updated"
          :type (first (:args request))
          :body response}})

(defview #'actions/get-page :json
  [request response]
  {:body response})

(defview #'actions/get-sub-page :json
  [request response]
  {:body response})

(defview #'actions/confirm :html
  [request response]
  {:body
   [:div
    [:p "Confirm"]
    (link-to (:item response))
    ]

   })

