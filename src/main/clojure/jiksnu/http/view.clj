(ns jiksnu.http.view
  (:use ciste.view
        hiccup.core
        [jiksnu.config :only (config)]
        jiksnu.session
        jiksnu.view
        ciste.core)
  (:require [clojure.pprint :as pprint]
            [hiccup.form-helpers :as f]))

(defn dump
  [val]
  (if (-> (config) :debug)
    [:p
     [:code
      [:pre
       (escape-html
        (with-out-str
          (pprint/pprint val)))]]]))

(defn dump-unescaped
  [val]
  (if (-> (config) :debug)
    [:p
     [:pre
      [:code.prettyprint
       (escape-html
        val)]]]))

(defn navigation-section
  []
  (let [user (current-user)]
    [:nav
    [:ul
     [:li [:a {:href "/"} "Home"]]
     (if user
       (list
        [:li [:a {:href "/settings/profile"} "Profile"]]
        (if (is-admin? user)
          (list
           [:li [:a {:href "/admin/users"} "Users"]]
           [:li [:a {:href "/admin/subscriptions"} "Subscriptions"]])))
       (list
        [:li [:a {:href "/register"} "Register"]]))]]))

(defn login-uri
  []
  "/login")

(defn logout-uri
  []
  "/logout")

(defn login-form
  []
  [:div
   (f/form-to [:post (login-uri)]
              (f/label :username "Username")
              (f/text-field :username)
              (f/label :password "Password")
              (f/password-field  :password)
              (f/submit-button "Login"))])

(defn logout-form
  []
  [:div
   (f/form-to
    [:post (logout-uri)]
    [:p "Logged in as:"
     (link-to (current-user))
     (f/submit-button "Logout")])])

(defn login-section
  []
  [:div
   (if-let [user (current-user)]
     (logout-form)
     (login-form))])

(defn link-to-script
  [href]
  [:script
   {:type "text/javascript"
    :lang "javascript"
    :src href}])

(defn link-to-stylesheet
  [href]
  [:link
   {:type "text/css"
    :href href
    :rel "stylesheet"
    :media "screen"}])

(defmethod serialize-as :http
  [serialization response-map]
  (merge {:headers {"Content-Type" "text/html"}}
         response-map
         (if-let [body (:body response-map)]
           {:body (html body)})))

(defn page-template-content
  [response]
  {:headers {"Content-Type" "text/html"}
   :body
   (str
    "<!doctype html>\n"
    (html
     [:html
      [:head
       [:title "jiksnu"]
       (map
        (fn [link]
          [:link {:type "application/atom+xml"
                  :href link
                  :rel "alternate"}])
        (:links response))
       #_(link-to-stylesheet "/public/css/smoothness/jquery-ui-1.8.4.custom.css")
       (link-to-script "http://code.jquery.com/jquery-1.4.4.min.js")
       #_(link-to-script "/public/js/jquery-ui-1.8.4.custom.min.js")
       (link-to-script "/public/standard.js")
       (link-to-stylesheet "/public/standard.css")]
      [:body
       [:header
        (navigation-section)
        (login-section)]
       [:div#content
        (:body response)]]]))})

(defmethod apply-template :html
  [request response]
  (merge
   response
   (if-not (= (:template response) false)
     (page-template-content response))))

(defsection link-to :default
  [record & options]
  (let [options-map (apply hash-map options)]
    [:a
     (apply merge {:href (uri record)} options-map)
     [:span {:about (uri record)
             :property "dc:title"}
      (or (:title options-map) (title record))] ]))
