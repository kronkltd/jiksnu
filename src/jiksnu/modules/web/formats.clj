(ns jiksnu.modules.web.formats
  (:require [ciste.core :refer [with-format]]
            [ciste.formats :refer [format-as]]
            [clojure.data.json :as json]
            [hiccup.core :as h]))

(defmethod format-as :clj
  [format request response]
  (-> response
      (assoc-in  [:headers "Content-Type"] "text/plain")
      (assoc :body (:body response))))

(defmethod format-as :html
  [format request response]
  (-> response
      (assoc :body (h/html (:body response)))))

(defmethod format-as :json
  [format request response]
  (-> response
      (assoc-in [:headers "Content-Type"] "application/json")
      (assoc :body (json/json-str (:body response)))))

(defmethod format-as :text
  [request format response]
  (-> response
      (assoc-in [:headers "Content-Type"] "text/plain; charset=utf-8")))

(defmethod format-as :xrd
  [format request response]
  (-> response
      (assoc-in [:headers "Content-Type"] "application/xrds+xml")
      (assoc-in [:headers "Access-Control-Allow-Origin"] "*")
      (assoc :body (h/html (:body response)))))

(defmethod format-as :xml
  [format request response]
  (-> response
      (assoc-in [:headers "Content-Type"] "application/xml")
      (assoc :body (h/html (:body response)))))

(defmethod format-as :xmpp
  [format request response]
  response)
