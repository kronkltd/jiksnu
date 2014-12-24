(ns jiksnu.modules.admin.views.like-views
  (:use [ciste.views :only [defview]]
        [jiksnu.modules.admin.actions.like-actions :only [delete index]]
        [jiksnu.modules.core.sections :only [admin-index-section]]
        [jiksnu.modules.web.sections :only [format-page-info redirect]])
  (:require [clojure.tools.logging :as log])
  (:import jiksnu.model.Like))

(defview #'index :html
  [request {:keys [items] :as page}]
  {:single true
   :title "Likes"
   :body (admin-index-section items page)})

(defview #'index :viewmodel
  [request {:keys [items] :as page}]
  {:body {:title "Likes"
          :pages {:likes (format-page-info page)}
          :likes (admin-index-section items page)}})

(defview #'delete :html
  [request _]
  (redirect "/admin/likes" "like deleted"))
