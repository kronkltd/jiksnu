(ns jiksnu.modules.core.views
  (:require [ciste.core :refer [serialize-as with-format]]
            [ciste.formats :refer [format-as]]
            [ciste.views :refer [defview]]
            [ciste.sections.default :refer [link-to]]
            [jiksnu.modules.core.actions :as actions])
  (:import (org.apache.http HttpStatus)))

(defn command-not-found
  []
  "Command not found")

(defmethod serialize-as :http
  [serialization response]
  (-> response
      (assoc :status HttpStatus/SC_OK)
      (update-in [:headers "Content-Type"] #(or % "text/html; charset=utf-8"))))

(defmethod serialize-as :command
  [serialization response]
  response)

(defmethod serialize-as :page
  [serialization response]
  response)

(defview #'actions/get-model :clj
  [request response]
  {:body {:action "model-updated"
          :type (first (:args request))
          :body response}})
