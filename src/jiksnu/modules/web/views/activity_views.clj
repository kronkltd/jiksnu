(ns jiksnu.modules.web.views.activity-views
  (:require [ciste.config :refer [config]]
            [ciste.views :refer [defview]]
            [ciste.sections.default :refer [show-section uri]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.modules.web.sections.activity-sections :as sections.activity]
            [jiksnu.session :as session]
            [jiksnu.modules.web.sections :refer [bind-to redirect]])
  (:import jiksnu.model.Activity))

(defview #'actions.activity/delete :html
  [request activity]
  (redirect "/"))

(defview #'actions.activity/edit :html
  [request activity]
  (let [actor (session/current-user)]
    (redirect (uri actor))))

(defview #'actions.activity/oembed :json
  [request oembed-map]
  {:status 200
   :body oembed-map})

(defview #'actions.activity/post :html
  [request activity]
  (let [actor (session/current-user)
        url (or (-> request :params :redirect_to)
                "/"
                (uri actor))]
    (redirect url)))

(defview #'actions.activity/show :html
  [request activity]
  {:body
   (let [activity (Activity.)]
     [:div
      (bind-to "targetActivity"
               (show-section activity))])})

(defview #'actions.activity/show :json
  [request activity]
  {:body (show-section activity)})

