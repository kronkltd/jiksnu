(ns jiksnu.modules.admin.views.like-views
  (:require [ciste.views :refer [defview]]
            [clojure.tools.logging :as log]
            [jiksnu.modules.admin.actions.like-actions :refer [delete index]]
            [jiksnu.modules.core.sections :refer [admin-index-section
                                                  format-page-info]])
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
