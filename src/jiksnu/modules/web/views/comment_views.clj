(ns jiksnu.modules.web.views.comment-views
  (:require [ciste.views :refer [defview]]
            [ciste.sections.default :refer [uri]]
            [jiksnu.actions.comment-actions :as actions.comment]
            [jiksnu.modules.web.sections :refer [redirect]]))

(defview #'actions.comment/add-comment :html
  [request activity]
  (redirect "/"))

(defview #'actions.comment/comment-response :html
  [request activity]
  (redirect "/"))

(defview #'actions.comment/fetch-comments :html
  [request [activity comments]]
  (redirect (uri activity)
            "comments are being fetched"))

