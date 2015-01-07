(ns jiksnu.modules.core.views
  (:use [ciste.core :only [serialize-as with-format]]
        [ciste.config :only [config]]
        [ciste.formats :only [format-as]]
        [ciste.views :only [defview]]
        [ciste.sections :only [defsection]]
        [ciste.sections.default :only [link-to
                                       index-line edit-button]]
        [jiksnu.session :only [current-user]])
  (:require [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            [jiksnu.actions :as actions]
            [jiksnu.model :as model]))

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
  (json/read-str (:body response) :key-fn keyword))

;; confirm

(defview #'actions/confirm :html
  [request response]
  {:body
   [:div
    [:p "Confirm"]
    (link-to (:item response))
    ]

   })

(defview #'actions/get-model :clj
  [request response]
  {:body {:action "model-updated"
          :type (first (:args request))
          :body response}})
