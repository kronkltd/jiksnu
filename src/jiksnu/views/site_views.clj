(ns jiksnu.views.site-views)

(defview #'rsd :html
  [request _]
  {:body
   [:rsd {:version "1.0"
          :xmlns "http://archipelago.phrasewise.com/rsd"}
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
             :blogId ""}]]]]})

(defview #'service :html
  [request user]
  {:body
   [:service {:xmlns "http://www.w3.org/2007/app"
              :xmlns:atom "http://www.w3.org/2005/Atom"
              :xmlns:activity ns/as}

    [:workspace
     [:atom:title "Main"]

     [:collection {:href (str "http://" (config :domain)
                              "/api/statuses/user_timeline/"
                              (:_id user) ".atom")}
      [:atom:title (str (:username user) " timeline")]
      [:accept "application/atom+xml;type=entry"]
      [:activity:verb "http://activitystrea.ms/schema/1.0/post"]]

     
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
      [:activity:verb "http://activitystrea.ms/schema/1.0/join"]]]]})

