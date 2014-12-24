(ns jiksnu.modules.web.views.user-views
  (:require [ciste.core :refer [with-format]]
            [ciste.views :refer [defview]]
            [ciste.sections.default :refer [uri index-section show-section]]
            [clojure.tools.logging :as log]
            [hiccup.core :as h]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.modules.web.sections.user-sections :as sections.user]
            [jiksnu.modules.web.sections :refer [bind-to pagination-links with-page]]
            [ring.util.response :as response]))

(defview #'actions.user/add-stream :html
  [request [user stream]]
  (-> (response/redirect-after-post (uri user))
      (assoc :template false)
      (assoc :flash "stream has been created")))

(defview #'actions.user/create :html
  [request user]
  (-> (response/redirect-after-post (uri user))
      (assoc :template false)
      (assoc :flash "user has been created")))

(defview #'actions.user/delete :html
  [request _]
  (-> (response/redirect-after-post "/")
      (assoc :template false)
      (assoc :flash "user has been deleted")))

(defview #'actions.user/discover :html
  [request _]
  (-> (response/redirect-after-post "/users")
      (assoc :template false)
      (assoc :flash "discovering user")))

(defview #'actions.user/index :html
  [request {:keys [items] :as page}]
  {:title "Users"
   :body (with-page "users"
           (pagination-links page)
           (index-section items page))})

(defview #'actions.user/profile :html
  [request user]
  {:title "Edit Profile"
   :body [:div (sections.user/edit-form user)]})

(defview #'actions.user/register :html
  [request user]
  (-> (response/redirect-after-post "/")
      (assoc :template false)
      (assoc :flash "user has been created")
      (assoc :session {:id (:_id user)})))

(defview #'actions.user/show :html
  [request user]
  {:template false
   :body (with-format :as
           (show-section user))})

(defview #'actions.user/update :html
  [request user]
  {:status 302
   :template false
   :flash "User updated"
   :headers {"Location" (uri user)}})

(defview #'actions.user/update-profile :html
  [request user]
  (-> (response/redirect-after-post "/main/profile")
      (assoc :template false)
      (assoc :flash "Profile updated")))

