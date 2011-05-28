(ns jiksnu.filters.activity-filters
  (:use aleph.http
        ciste.debug
        ciste.filters
        clj-tigase.core
        jiksnu.abdera
        jiksnu.actions.activity-actions
        jiksnu.helpers.activity-helpers
        jiksnu.model
        jiksnu.sections.activity-sections
        jiksnu.session
        lamina.core)
  (:require [jiksnu.model.activity :as model.activity]
            [jiksnu.model.like :as model.like]
            [jiksnu.model.user :as model.user]))

(deffilter #'comment-response :xmpp
  [action request]
  (if (not= (:to request) (:from request))
    (let [packet (:packet request)
          items (:items request)]
      (action (map #(to-activity
                     (parse-xml-string
                      (str (first (children %)))))
                   items)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; delete
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deffilter #'delete :http
  [action request]
  (let [{{id :id} :params} request]
    (action id)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; edit
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deffilter #'edit :http
  [action request]
  (let [{{id :id} :params} request]
    (action id)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; fetch-comments
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deffilter #'fetch-comments :http
  [action request]
  (let [{{id :id} :params} request]
    (if-let [activity (model.activity/show id)]
      (action activity))))

(deffilter #'fetch-comments :xmpp
  [action request]
  (let [{{id :id} :params} request]
    (if-let [activity (model.activity/show id)]
      (action activity))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; fetch-comments-remote
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deffilter #'fetch-comments-remote :xmpp
  [action request])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; friends-timeline
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deffilter #'friends-timeline :http
  [action request]
  (let [{{id :id} :params} request]
    (model.activity/index :authors id)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; inbox
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deffilter #'inbox :http
  [action request]
  [])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; index
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deffilter #'index :http
  [action request]
  (action))

(deffilter #'index :xmpp
  [action request]
  (action))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; like-activity
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deffilter #'like-activity :http
  [action request]
  (let [{{id :id} :params} request]
    (if-let [user (current-user)]
      (if-let [activity (model.activity/fetch-by-id id)]
        (model.like/find-or-create activity user)
        true))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; new
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deffilter #'new :http
  [action request]
  (action))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; new-comment
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deffilter #'new-comment :http
  [action request]
  (let [{{id :id} :params} request]
    (model.activity/show id)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; post
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deffilter #'post :http
  [action request]
  (let [{params :params} request]
    (action (dissoc params :*))))

(deffilter #'post :xmpp
  [action request]
  (let [{:keys [items]} request
        activities
        (map
         (fn [item]
           (-> item children first
               str parse-xml-string
               to-activity))
         items)]
    (action (first activities))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; show
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deffilter #'show :http
  [action request]
  (let [{{id :id} :params} request]
    (action id)))

(deffilter #'show :xmpp
  [action request]
  (let [{:keys [items]} request
        ids (map #(.getAttribute % "id") items)
        id (first ids)]
    (action id)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; stream
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; remote-create
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deffilter #'remote-create :xmpp
  [action request]
  (if (not= (:to request) (:from request))
    (let [packet (:packet request)
          ;; items (children packet "/message/event/items/item")
          items (map (comp first children) (:items request))]
      (action (map #(to-activity (parse-xml-string (str %)))
            items)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; update
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deffilter #'update :http
  [action request]
  (let [{params :params} request]
    (action params)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; user-timeline
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deffilter #'user-timeline :http
  [action request]
  (let [{{id :id} :params} request]
    (let [user (model.user/fetch-by-id id)]
      (action user))))

;; (deffilter #'user-timeline :http
;;   [action request]
;;   (let [{{id :id} :params} request
;;         user (model.user/fetch-by-id id)]
;;     [user (model.activity/index :authors (make-id id))]))

(deffilter #'user-timeline :xmpp
  [action request]
  (let [user (model.user/fetch-by-jid (:to request))]
    (action user)))
