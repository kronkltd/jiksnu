(ns jiksnu.actions.admin.setting-actions
  (:use [ciste.config :only [config definitializer set-config! write-config!]]
        [ciste.core :only [defaction]]
        [ciste.runner :only [require-namespaces]])
  (:require [jiksnu.session :as session]))

(defaction edit-page
  []
  (session/is-admin?))

(defaction update-settings
  [params]
  (let [site-name (get params "site.name")
        domain (:domain params)
        admin-email (get params "site-email")
        print-actions (= "on" (get params "print.actions"))
        print-triggers (= "on" (get params "print.triggers"))
        print-request (= "on" (get params "print.request"))
        print-routes (= "on" (get params "print.routes"))
        registration-enabled (= "on" (get params "registration-enabled"))]
    (set-config! [:site :name] site-name)
    (set-config! [:domain] domain)
    (set-config! [:site :email] admin-email)
    (set-config! [:print :actions] print-actions)
    (set-config! [:print :request] print-request)
    (set-config! [:print :routes] print-routes)
    (set-config! [:print :triggers] print-triggers)
    (set-config! [:registration-enabled] registration-enabled)
    (write-config!)
    params))

(definitializer
  (require-namespaces
   ["jiksnu.filters.admin.setting-filters"
    "jiksnu.views.admin.setting-views"]))
