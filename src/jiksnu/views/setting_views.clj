(ns jiksnu.views.setting-views
  (:use ciste.views
        jiksnu.actions.setting-actions)
  (:require [hiccup.form-helpers :as f]
            (jiksnu.templates [setting :as templates.setting])))

(defview #'admin-edit-page :html
  [request _]
  {:body (templates.setting/edit-page)})
