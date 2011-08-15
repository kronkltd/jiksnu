(ns jiksnu.views.settings-views
  (:use ciste.views
        jiksnu.actions.settings-actions)
  (:require [hiccup.form-helpers :as f]
            (jiksnu.templates [settings :as templates.settings])))

(defview #'edit :html
  [request _]
  {:body (templates.settings/edit-page)})
