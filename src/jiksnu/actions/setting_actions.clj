(ns jiksnu.actions.setting-actions
  (:use [ciste.config :only [config definitializer set-config! write-config!]]
        [ciste.core :only [defaction]]
        [ciste.debug :only [spy]]
        [ciste.runner :only [require-namespaces]])
  (:require [jiksnu.session :as session]))

(defaction admin-edit-page
  []
  
  (session/is-admin?)


  )

(defaction oauth-apps
  []
  
  )

(defaction config-output
  []
  {:site
   {
    :name (config :site :name)
    :server (config :domain)
    ;; TODO: theme name
    :theme "classic"
    ;; TODO: logo
    :logo ""
    :fancy "1"
    ;; TODO: default language
    :language "en"
    ;; TODO: email
    :email "admin@domain.com"
    :broughtby "Jiksnu"
    :broughtbyurl "http://jiksnu.org/"
    :timezone "UTC"
    :closed "0"
    :inviteonly "0"
    :private "0"
    :textlimit "140"
    :ssl "sometimes"
    :sslserver "renfer.name"
    :shorturllength 30
    }
   :license {
             :type "cc"
             :owner nil
             :url "http://creativecommons.org/licenses/by/3.0/"
             :title "Creative Commons Attribution 3.0",
             :image "http://i.creativecommons.org/l/by/3.0/80x15.png"
             }
   :nickname {
              :featured ["daniel"]

              }
   :profile {:biolimit nil}
   :group {:desclimit nil}
   :notice {:contentlimit nil}
   :throttle {
              :enabled true
              :count 20
              :timespan 600
              
              }
   :xmpp {
          :enabled true
          :server (config :domain)
          :port 5222
          :user "update"
          }
   :integration {:source "jiksnu"}
   :attachments {
                 :upload true
                 :file_quota 2097152
                 }
   })

(defn avatar-page
  [user]
  {:user user})

(defaction update-settings
  [params]
  (let [site-name (get params "site.name")
        domain (:domain params)
        admin-email (get params "site-email")
        print-actions (= "on" (get params "print.actions"))
        registration-enabled (= "on" (get params "registration-enabled"))
        ]
    (set-config! [:site :name] site-name)
    (set-config! [:domain] domain)
    (set-config! [:site :email] admin-email)
    (set-config! [:print :actions] print-actions)
    (set-config! [:print :request] (= "on" (get params "print.request")))
    (set-config! [:print :routes] (= "on" (get params "print.routes")))
    (set-config! [:registration-enabled] registration-enabled)
    (write-config!)
    (spy params)))


(definitializer
  (require-namespaces
   ["jiksnu.filters.setting-filters"
    "jiksnu.views.setting-views"]))
