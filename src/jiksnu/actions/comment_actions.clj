(ns jiksnu.actions.comment-actions
  (:require [ciste.core :refer [defaction]]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.user :as model.user]
            [jiksnu.namespace :as ns]))

;; TODO: What id should be used here?
(defn comment-node-uri
  [{id :id}]
  (str ns/microblog ":replies:item=" id))

(defaction add-comment
  [params]
  (if-let [parent (model.activity/fetch-by-id (:id params))]
    (actions.activity/post
     (-> params
         (assoc :parent (:_id parent))
         (assoc-in [:object :type] "comment")))))

(defaction comment-response
  [activities]
  (actions.activity/remote-create activities))

;; TODO: fetch all in 1 request
(defn fetch-comments
  [activity]
  (let [comments (map model.activity/fetch-by-id
                      (:comments activity))]
    [activity comments]))
