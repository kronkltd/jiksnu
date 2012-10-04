(ns jiksnu.views.site-views
  (:use [ciste.config :only [config]]
        [ciste.views :only [defview]]
        jiksnu.actions.site-actions)
  (:require [hiccup.core :as h]
            [jiksnu.namespace :as ns]))

(defview #'rsd :html
  [request _]
  {:template false
   :headers {"Content-Type" "application/xml"}
   :body
   (h/html [:rsd {:version "1.0"
                  :xmlns ns/rsd}
            [:service
             [:engineName "Jiksnu"]
             [:engineLink "http://jiksnu.org/"]
             [:apis
              [:api {:name "Twitter"
                     :preferred "true"
                     :apiLink (str "http://" (config :domain) "/api/")
                     :blogId ""}
               [:settings
                [:docs "http://status.net/wiki/TwitterCompatibleAPI"]
                [:setting {:name "OAuth"}
                 ;; TODO: Make this true
                 "false"]]]
              [:api {:name "Atom"
                     :preferred "false"
                     :apiLink (str "http://" (config :domain) "/api/statusnet/app/service.xml")
                     :blogId ""}]]]])})

(defview #'service :xml
  [request user]
  {:template false
   :body
   (h/html
    [:service {:xmlns          ns/app
               :xmlns:atom     ns/atom
               :xmlns:activity ns/as}

     [:workspace
      [:atom:title "Main"]

      [:collection {:href (str "http://" (config :domain)
                               "/api/statuses/user_timeline/"
                               (:_id user) ".atom")}
       [:atom:title (str (:username user) " timeline")]
       [:accept "application/atom+xml;type=entry"]
       [:activity:verb ns/post]]

     
      [:collection {:href (str "http://" (config :domain)
                               "/api/statusnet/app/subscriptions/"
                               (:_id user) ".atom")}
       [:atom:title (str (:username user) " subscriptions")]
       [:accept "application/atom+xml;type=entry"]
       [:activity:verb "http://activitystrea.ms/schema/1.0/follow"]]


      [:collection {:href (str "http://" (config :domain)
                               "/api/statusnet/app/favorites/"
                               (:_id user) ".atom")}
       [:atom:title (str (:username user) " favorites")]
       [:accept "application/atom+xml;type=entry"]
       [:activity:verb "http://activitystrea.ms/schema/1.0/favorite"]]

      [:collection {:href (str "http://" (config :domain)
                               "/api/statusnet/app/memberships/"
                               (:_id user) ".atom")}
       [:atom:title (str (:username user) " memberships")]
       [:accept "application/atom+xml;type=entry"]
       [:activity:verb "http://activitystrea.ms/schema/1.0/join"]]]])})


(defview #'get-stats :json
  [request stats]
  {:status 200
   :type "stats-updated"
   ;; :template false
   :body {:type "status-updated"
          :body stats}})


(defview #'get-environment :text
  [request data]
  {:body data})

(defview #'get-environment :json
  [request data]
  {:body data})

(defview #'get-config :text
  [request data]
  {:body data})

(defview #'get-config :json
  [request data]
  {:body data})

(defview #'ping :text
  [request data]
  {:body data})

(defview #'ping :json
  [request data]
  {:body data})

;; (defview #'get-load :text
;;   [request data]
;;   data)


