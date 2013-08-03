(ns jiksnu.modules.core.views
  (:use [ciste.core :only [serialize-as with-format]]
        [ciste.config :only [config]]
        [ciste.formats :only [format-as]]
        [ciste.views :only [defview]]
        [ciste.sections :only [defsection]]
        [ciste.sections.default :only [title link-to
                                       index-line edit-button]]
        [jiksnu.session :only [current-user]])
  (:require [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            [hiccup.core :as h]
            [jiksnu.actions :as actions]
            [jiksnu.model :as model]
            [plaza.rdf.core :as rdf]))

(defn command-not-found
  []
  "Command not found")




(defmethod serialize-as :http
  [serialization response-map]
  (let [content-type (or (-> response-map :headers (get "Content-Type"))
                         "text/html; charset=utf-8")]
    (-> (merge {:status 200} response-map)
        (assoc-in  [:headers "Content-Type"] content-type))))

(defmethod serialize-as :command
  [serialization response]
  response)

(defmethod serialize-as :page
  [serialization response]
  (json/read-json (:body response)))

;; confirm

(defview #'actions/confirm :html
  [request response]
  {:body
   [:div
    [:p "Confirm"]
    (link-to (:item response))
    ]

   })

;; connect

(defview #'actions/connect :json
  [request response]
  {:body {:action "connect"
          :connection-id response}})

(defview #'actions/get-model :json
  [request response]
  {:body {:action "model-updated"
          :type (first (:args request))
          :body response}})

(defview #'actions/get-model :clj
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

;; invoke-action

(defview #'actions/invoke-action :json
  [request data]
  {:body data})

