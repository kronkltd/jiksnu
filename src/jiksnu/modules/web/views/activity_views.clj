(ns jiksnu.modules.web.views.activity-views
  (:require [ciste.views :refer [defview]]
            [ciste.sections.default :refer [uri]]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.session :as session]
            [jiksnu.modules.web.sections :refer [redirect]]))

(defview #'actions.activity/delete :html
  [request activity]
  (redirect "/"))

(defview #'actions.activity/edit :html
  [request activity]
  (let [actor (session/current-user)]
    (redirect (uri actor))))

(defview #'actions.activity/post :html
  [request activity]
  (let [actor (session/current-user)
        url (or (-> request :params :redirect_to)
                "/"
                (uri actor))]
    (redirect url)))
