(ns jiksnu.actions.setting-actions
  (:use (ciste [config :only [definitializer]]
               [core :only [defaction]]))
  (:require (jiksnu [session :as session])))

(defaction admin-edit-page
  []
  (session/is-admin?))

(defaction oauth-apps
  []
  
  )

(defn avatar-page
  [user]
  {:user user})


(definitializer
  (doseq [namespace ['jiksnu.filters.setting-filters
                     'jiksnu.views.setting-views]]
    (require namespace)))
