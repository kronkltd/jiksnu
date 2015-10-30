(ns jiksnu.modules.admin.actions.setting-actions
  (:require [ciste.config :refer [set-config! write-config!]]
            [jiksnu.session :as session]))

(defn edit-page
  []
  (session/is-admin?))

(defn update-settings
  [params]
  (let [site-name (get params "site.name")
        domain (:domain params)
        admin-email (get params "site-email")
        print-actions (= "on" (get params "print.actions"))
        print-triggers (= "on" (get params "print.triggers"))
        print-request (= "on" (get params "print.request"))
        print-routes (= "on" (get params "print.routes"))
        registration-enabled (= "on" (get params "registration-enabled"))
        htmlOnly (= "on" (get params :htmlOnly))]
    (set-config! [:site :name] site-name)
    (set-config! [:domain] domain)
    (set-config! [:site :email] admin-email)
    (set-config! [:print :actions] print-actions)
    (set-config! [:print :request] print-request)
    (set-config! [:print :routes] print-routes)
    (set-config! [:print :triggers] print-triggers)
    (set-config! [:registration-enabled] registration-enabled)
    (set-config! [:htmlOnly] htmlOnly)
    (write-config!)
    params))
