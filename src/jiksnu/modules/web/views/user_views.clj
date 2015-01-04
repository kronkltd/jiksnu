(ns jiksnu.modules.web.views.user-views
  (:require [ciste.core :refer [with-format]]
            [ciste.views :refer [defview]]
            [ciste.sections.default :refer [uri index-section show-section]]
            [clojure.tools.logging :as log]
            [hiccup.core :as h]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.modules.web.sections.user-sections :as sections.user]
            [jiksnu.modules.web.sections :refer [pagination-links
                                                 redirect]]))

(defview #'actions.user/add-stream :html
  [request [user stream]]
  (redirect (uri user) "stream has been created"))

(defview #'actions.user/create :html
  [request user]
  (redirect (uri user) "user has been created"))

(defview #'actions.user/delete :html
  [request _]
  (redirect "/" "user has been deleted"))

(defview #'actions.user/discover :html
  [request _]
  (redirect "/users" "discovering user"))

(defview #'actions.user/index :html
  [request {:keys [items] :as page}]
  {:title "Users"
   :body (index-section items page)})

(defview #'actions.user/register :html
  [request user]
  (-> (redirect "/" "user has been created")
      (assoc :session {:id (:_id user)})))

(defview #'actions.user/show :html
  [request user]
  {:template false
   :body (with-format :as
           (show-section user))})

(defview #'actions.user/update :html
  [request user]
  (redirect (uri user) "User updated"))

(defview #'actions.user/update-profile :html
  [request user]
  (redirect "/main/profile" "Profile updated"))

