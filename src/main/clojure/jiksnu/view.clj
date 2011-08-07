(ns jiksnu.view
  (:use [ciste core debug formats html sections views]
        ciste.sections.default
        [ciste.config :only (config *environment*)]
        clj-tigase.core
        jiksnu.abdera
        jiksnu.helpers.auth-helpers
        jiksnu.model
        jiksnu.namespace
        jiksnu.session
        jiksnu.xmpp
        jiksnu.xmpp.element
        [karras.entity :only (make)])
  (:require [clojure.tools.logging :as log]
            [clojure.stacktrace :as stacktrace]
            [ciste.debug :as debug]
            [hiccup.core :as hiccup]
            [hiccup.form-helpers :as f]
            (jiksnu.templates
             [layout :as template.layout]
             [user :as template.user]))
  (:import com.cliqset.abdera.ext.activity.ActivityEntry
           javax.xml.namespace.QName
           jiksnu.model.Activity
           tigase.server.Packet
           tigase.xml.Element
           tigase.xmpp.StanzaType))

(defsection link-to :default
  [record & options]
  (let [options-map (apply hash-map options)]
    [:a
     (apply merge {:href (uri record)} options-map)
     [:span {:about (uri record)
             :property "dc:title"}
      (or (:title options-map) (title record))] ]))

(defsection full-uri :default
  [record & options]
  (str "http://" (config :domain)
       (apply uri record options)))

(defn page-template-content
  [response]
  {:headers {"Content-Type" "text/html"}
   :body
   (str
    "<!doctype html>\n"
    (template.layout/layout response))})

(defmethod apply-template :html
  [request response]
  (merge
   (dissoc response :formats)
   (if (not= (:template response) false)
     (page-template-content response))))

(defmethod apply-view-by-format :atom
  [request response])

(defmethod format-as :xmpp
  [format request response]
  response)

(defmethod format-as :html
  [format request response]
  response)

(defmethod serialize-as :http
  [serialization response-map]
  (assoc-in
   (merge {:status 200}
          response-map
          (if-let [body (:body response-map)]
            {:body body}))
   [:headers "Content-Type"]
   (or (-> response-map :headers (get "Content-Type"))
       "text/html; charset=utf-8")))

(defmethod serialize-as :xmpp
  [serialization response]
  (if response
    (make-packet (spy response))))

(defn get-text
  [element]
  (if element
    (.getCData element)))
