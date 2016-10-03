(ns jiksnu.modules.admin.actions.setting-actions
  (:require [jiksnu.session :as session]))

(defn edit-page
  []
  (session/is-admin?))

