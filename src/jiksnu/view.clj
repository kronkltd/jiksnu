(ns jiksnu.view
  (:use (ciste core
               [config :only (config)]
               [debug :only (spy)]
               formats html sections views)
        ciste.sections.default
        (jiksnu model session))
  (:require (clj-tigase [core :as tigase])
            (jiksnu [namespace :ads namespace]
                    [xmpp :as xmpp])
            (jiksnu.templates [layout :as templates.layout]
                              [user :as templates.user])
            (jiksnu.xmpp [element :as element])))

(defsection link-to :default
  [record & options]
  (let [options-map (apply hash-map options)]
    [:a
     (apply merge {:href (uri record)} options-map)
     [:span {:about (uri record)
             :property "dc:title"}
      (or (:title options-map) (title record))] ]))

(defn page-template-content
  [response]
  {:headers {"Content-Type" "text/html; charset=utf-8"}
   :body
   (str
    "<!doctype html>\n"
    (templates.layout/layout response))})

(defmethod apply-template :html
  [request response]
  (merge response
         (if (not= (:template response) false)
           (page-template-content
            (if (:flash request)
              (assoc response :flash (:flash request))
              response)))))

(defmethod apply-view-by-format :atom
  [request response])

(defmethod serialize-as :http
  [serialization response-map]
  (assoc-in
   (merge {:status 200} response-map)
   [:headers "Content-Type"]
   (or (-> response-map :headers (get "Content-Type"))
       "text/html; charset=utf-8")))

(defmethod serialize-as :xmpp
  [serialization response]
  (if response
    (tigase/make-packet response)))
