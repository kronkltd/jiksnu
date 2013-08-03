(ns jiksnu.modules.core.views.comment-views
  (:use [ciste.views :only [defview]]
        [ciste.sections.default :only [uri]]
        [clojurewerkz.route-one.core :only [named-path]]
        jiksnu.actions.comment-actions)
  (:require [clj-tigase.core :as tigase]
            [clj-tigase.element :as element]
            [clj-tigase.packet :as packet]
            [jiksnu.namespace :as ns]
            [ring.util.response :as response]))

;; add-comment

(defview #'add-comment :html
  [request activity]
  (-> (named-path "public timeline")
      response/redirect-after-post
      (assoc :template false)))

;; comment-response

(defview #'comment-response :html
  [request activity]
  (-> (named-path "public timeline")
      response/redirect-after-post
      (assoc :template false)))

;; fetch-comments

(defview #'fetch-comments :html
  [request [activity comments]]
  (-> (response/redirect-after-post (uri activity))
      (assoc :template false)
      (assoc :flash "comments are being fetched")))

