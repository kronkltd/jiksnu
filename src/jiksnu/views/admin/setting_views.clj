(ns jiksnu.views.setting-views
  (:use [ciste.views :only [defview]]
        [jiksnu.actions.admin.setting-actions :only [edit-page update-settings]])
  (:require [jiksnu.sections.setting-sections :as sections.setting]))

(defview #'edit-page :html
  [request _]
  {:body (sections.setting/edit-form)})

(defview #'update-settings :html
  [request data]
  {:flash "Settings updated"
   :status 303
   :template false
   :headers {"Location" "/admin/settings"}})
