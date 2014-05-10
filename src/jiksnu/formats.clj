(ns jiksnu.formats
  (:require [ciste.core :refer [with-format]]
            [ciste.formats :refer [format-as]]
            [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            [hiccup.core :as h]
            [jiksnu.session :as session]))

(defmethod format-as :as
  [format request response]
  (with-format :json (format-as :json request response)))

(defmethod format-as :clj
  [format request response]
  (-> response
      (assoc-in  [:headers "Content-Type"] "text/plain")
      (assoc :body (:body response))))

;; (defmethod format-as :default
;;   [format request response]
;;   response)

(defmethod format-as :html
  [format request response]
  (-> response
      (assoc :body (h/html (:body response)))))

(defmethod format-as :json
  [format request response]
  (-> response
      (assoc-in [:headers "Content-Type"] "application/json")
      (assoc :body (json/json-str (:body response)))))

(defmethod format-as :page
  [format request response]
  (with-format :json
    (doall (format-as :json request response))))

(defmethod format-as :model
  [format request response]
  (with-format :json
    (doall (format-as :json request response))))


(defmethod format-as :text
  [request format response]
  (-> response
      (assoc-in [:headers "Content-Type"] "text/plain; charset=utf-8")))

(defmethod format-as :viewmodel
  [format request response]
  (let [response (if-let [id (session/current-user-id)]
                   (assoc-in response [:body :currentUser] id)
                   response)]
    (with-format :json
      (doall (format-as :json request response)))))

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
