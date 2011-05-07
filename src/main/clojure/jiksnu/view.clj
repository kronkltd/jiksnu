(ns jiksnu.view
  (:use ciste.core
        ciste.debug
        ciste.formats
        ciste.html
        ciste.sections
        ciste.sections.default
        ciste.views
        [ciste.config :only (config)]
        clj-tigase.core
        jiksnu.abdera
        jiksnu.helpers.auth-helpers
        jiksnu.model
        jiksnu.namespace
        jiksnu.sections.auth-sections
        jiksnu.session
        jiksnu.xmpp
        jiksnu.xmpp.element)
  (:require [clojure.contrib.logging :as log]
            [clojure.stacktrace :as stacktrace]
            [ciste.debug :as debug]
            [hiccup.core :as hiccup]
            [hiccup.form-helpers :as f])
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
  (str "http://" (-> (config) :domain)
       (apply uri record options)))

(defn navigation-section
  []
  (let [user (current-user)]
    [:nav
    [:ul
     [:li [:a {:href "/"} "Public"]]
     (if user
       (list
        [:li [:a {:href (str (uri user) "/all")} "Home"]]
        [:li [:a {:href "/settings/profile"} "Profile"]]
        [:li [:a {:href "/domains"} "Domains"]]
        (if (is-admin? user)
          (list
           [:li [:a {:href "/admin/notices"} "Notices"]]
           [:li [:a {:href "/admin/settings"} "Settings"]]
           [:li [:a {:href "/admin/push/subscriptions"} "Pubsubhubbub"]]
           [:li [:a {:href "/admin/users"} "Users"]]
           [:li [:a {:href "/admin/subscriptions"} "Subscriptions"]])))
       (list
        [:li [:a {:href "/main/register"} "Register"]]))]]))

(defn devel-environment-section
  []
  [:div.important.devel-section
   "This site is running in development mode. No guarantees are made about the accuracy or security of information on this site. Use at your own risk."])

(defn page-template-content
  [response]
  {:headers {"Content-Type" "text/html"}
   :body
   (str
    "<!doctype html>\n"
    (hiccup/html
     [:html
      [:head
       [:title "jiksnu"]
       (map
        (fn [{:keys [label href type]}]
          [:link {:type type
                  :href href
                  :title label
                  :rel "alternate"}])
        (:formats response))
       #_(link-to-stylesheet
          "/public/css/smoothness/jquery-ui-1.8.4.custom.css")
       (link-to-stylesheet "/public/standard.css")]
      [:body
       [:div#wrap
        [:header
         (navigation-section)
         (login-section)]
        [:div#content
         (devel-environment-section)
         (:body response)]
        [:footer
         [:div.formats
          [:ul
           (map
            (fn [{:keys [label href type]}]
              [:li
               [:a {:href href} label]])
            (:formats response))]]
         [:p "Copyright © 2011 KRONK Ltd."]]]
       (link-to-script "http://code.jquery.com/jquery-1.4.4.min.js")
       #_(link-to-script "/public/js/jquery-ui-1.8.4.custom.min.js")
       (link-to-script "/public/standard.js")]]))})


(defmethod apply-template :html
  [request response]
  (merge
   response
   (if-not (= (:template response) false)
     (page-template-content response))))

(defmethod apply-view-by-format :atom
  [request response])

(defmethod format-as :xmpp
  [format request response]
  response)

(defmethod serialize-as :http
  [serialization response-map]
  (merge {:headers {"Content-Type" "text/html"}}
         response-map
         (if-let [body (:body response-map)]
           {:body body})))

(defmethod serialize-as :xmpp
  [serialization response]
  (if response
    (make-packet response)))

(defn get-text
  [element]
  (if element
    (.getCData element)))
