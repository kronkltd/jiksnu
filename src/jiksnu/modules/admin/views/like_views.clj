(ns jiksnu.modules.admin.views.like-views
  (:use [ciste.views :only [defview]]
        [jiksnu.modules.admin.actions.like-actions :only [delete index]]
        [jiksnu.ko :only [*dynamic*]]
        [jiksnu.modules.core.sections :only [admin-index-section]]
        [jiksnu.modules.web.sections :only [format-page-info
                                            pagination-links with-page]])
  (:require [clojure.tools.logging :as log])
  (:import jiksnu.model.Like))

(defview #'index :html
  [request {:keys [items] :as response}]
  {:single true
   :title "Likes"
   :body
   (with-page "likes"
     (list
      (pagination-links response)
      (admin-index-section
       (if *dynamic*
         (Like.)
         items) response)))})

(defview #'index :viewmodel
  [request {:keys [items] :as page}]
  {:body {:title "Likes"
          :pages {:likes (format-page-info page)}
          :likes (admin-index-section items page)}})

(defview #'delete :html
  [request _]
  {:status 303
   :flash "like deleted"
   :template false
   :headers {"Location" "/admin/likes"}})
