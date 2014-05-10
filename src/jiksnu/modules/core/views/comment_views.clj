(ns jiksnu.modules.core.views.comment-views
  (:require [ciste.views :refer [defview]]
            [ciste.sections.default :refer [uri]]
            [clj-tigase.core :as tigase]
            [clj-tigase.element :as element]
            [jiksnu.actions.comment-actions :as actions.comment]
            [jiksnu.namespace :as ns]
            [jiksnu.routes.helpers :refer [named-path]]
            [ring.util.response :as response]))

;; add-comment

(defview #'actions.comment/add-comment :html
  [request activity]
  (-> (named-path "public timeline")
      response/redirect-after-post
      (assoc :template false)))

;; comment-response

(defview #'actions.comment/comment-response :html
  [request activity]
  (-> (named-path "public timeline")
      response/redirect-after-post
      (assoc :template false)))

;; fetch-comments

(defview #'actions.comment/fetch-comments :html
  [request [activity comments]]
  (-> (response/redirect-after-post (uri activity))
      (assoc :template false)
      (assoc :flash "comments are being fetched")))

