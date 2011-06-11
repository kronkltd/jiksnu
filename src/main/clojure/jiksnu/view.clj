(ns jiksnu.view
  (:use [ciste core debug formats html sections views]
        ciste.sections.default
        [ciste.config :only (config)]
        clj-tigase.core
        jiksnu.abdera
        jiksnu.helpers.auth-helpers
        jiksnu.helpers.activity-helpers
        jiksnu.model
        jiksnu.namespace
        jiksnu.sections.auth-sections
        jiksnu.session
        jiksnu.xmpp
        jiksnu.xmpp.element)
  (:require [clojure.tools.logging :as log]
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
       (link-to-stylesheet "/public/standard.css")]
      [:body
       [:div#wrap
        (devel-environment-section)
        [:header
         [:address#site_contact.vcard
          [:a.url.home.bookmark
           {:href "/home"}
           "Jiksnu"
           [:a.logo.photo {:src "/public/logo.png"
                           :alt "Jiksnu"}]]]
         (login-section)
         (activity-form {})]
        (navigation-section)
        [:div#content
         (:body response)
         [:div.clear]]
        [:footer
         [:div.formats
          [:ul
           (map
            (fn [{:keys [label href type]}]
              [:li
               [:a {:href href} label]])
            (:formats response))]]
         [:p "Copyright © 2011 KRONK Ltd."]]]
       (link-to-script "http://www.google.com/jsapi")
       [:script "google.load(\"jquery\", \"1.3\")"]
       (link-to-script "/public/standard.js")]]))})

(defmethod apply-template :html
  [request response]
  (merge
   (dissoc response :formats)
   (if-not (= (:template response) false)
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
