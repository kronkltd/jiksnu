(ns jiksnu.modules.web.views.activity-views
  (:require [ciste.config :refer [config]]
            [ciste.views :refer [defview]]
            [ciste.sections.default :refer [show-section uri]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.modules.web.sections.activity-sections :as sections.activity]
            [jiksnu.session :as session]
            [jiksnu.modules.web.sections :refer [bind-to]]
            [ring.util.response :as response])
  (:import jiksnu.model.Activity))

(defview #'actions.activity/delete :html
  [request activity]
  (-> (response/redirect-after-post "/")
      (assoc :template false)))

(defview #'actions.activity/edit :html
  [request activity]
  (let [actor (session/current-user)]
    (-> (response/redirect-after-post (uri actor))
        (assoc :template false))))

(defview #'actions.activity/post :html
  [request activity]
  (let [actor (session/current-user)
        url (or (-> request :params :redirect_to)
                "/"
                (uri actor))]
    (-> (response/redirect-after-post url)
        (assoc :template false))))

(defview #'actions.activity/show :html
  [request activity]
  {:body
   (let [activity (Activity.)]
     [:div
      (bind-to "targetActivity"
               (show-section activity))])})
