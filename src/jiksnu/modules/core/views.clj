(ns jiksnu.modules.core.views
  (:require [ciste.core :refer [serialize-as with-format]]
            [ciste.formats :refer [format-as]]
            [ciste.views :refer [defview]]
            [ciste.sections :refer [defsection]]
            [ciste.sections.default :refer [link-to index-line edit-button]]
            [clojure.data.json :as json]
            [jiksnu.actions :as actions]
            [jiksnu.model :as model]))

(defn command-not-found
  []
  "Command not found")

(defmethod serialize-as :http
  [serialization response-map]
  (let [content-type (or (-> response-map :headers (get "Content-Type"))
                         "text/html; charset=utf-8")]
    (assoc-in
     (assoc response-map :status 200)
     [:headers "Content-Type"]
     content-type)))

(defmethod serialize-as :command
  [serialization response]
  response)

(defmethod serialize-as :page
  [serialization response]
  (:body response)
  #_(json/read-str (:body response) :key-fn keyword))

;; confirm

(defview #'actions/confirm :html
  [request response]
  {:body
   [:div
    [:p "Confirm"]
    (link-to (:item response))]})

(defview #'actions/get-model :clj
  [request response]
  {:body {:action "model-updated"
          :type (first (:args request))
          :body response}})
