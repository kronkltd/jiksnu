(ns jiksnu.modules.core.views.setting-views
  (:use [ciste.views :only [defview]]
        [jiksnu.modules.admin.actions.setting-actions :only [edit-page update-settings]])
  (:require [jiksnu.modules.web.sections.setting-sections :as sections.setting]))

(defview #'edit-page :html
  [request _]
  {:body (sections.setting/edit-form)})

(defview #'edit-page :viewmodel
  [request _]
  {:body {:title "Edit Settings"}})

(defview #'update-settings :html
  [request data]
  {:flash "Settings updated"
   :status 303
   :template false
   :headers {"Location" "/admin/settings"}})
