(ns jiksnu.views.admin.like-views
  (:use [ciste.views :only [defview]]
        [jiksnu.actions.admin.like-actions :only [delete index]]
        [jiksnu.sections :only [admin-index-section]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.sections.like-sections :as sections.like]))

(defview #'index :html
  [request {:keys [items] :as response}]
  {:single true
   :title "Likes"
   :body (admin-index-section items response)})

(defview #'delete :html
  [request _]
  {:status 303
   :flash "like deleted"
   :template false
   :headers {"Location" "/admin/likes"}})
