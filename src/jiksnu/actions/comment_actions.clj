(ns jiksnu.actions.comment-actions
  (:use (ciste [config :only [config definitializer]]
               [core :only [defaction]]
               [debug :only [spy]]))
  (:require (clj-tigase [core :as tigase]
                        [element :as element])
            (jiksnu [model :as model]
                    [namespace :as ns])
            (jiksnu.actions [activity-actions :as actions.activity])
            (jiksnu.model [activity :as model.activity]
                          [domain :as model.domain]
                          [user :as model.user])))

;; TODO: What id should be used here?
(defn comment-node-uri
  [{id :id :as activity}]
  (str ns/microblog ":replies:item=" id))

(defaction new-comment
  [& _])

(defaction add-comment
  [params]
  (if-let [parent (model.activity/fetch-by-id (:id params))]
    (actions.activity/post
     (-> params
         (assoc :parent (:_id parent))
         (assoc-in [:object :object-type] "comment")))))

(defaction comment-response
  [activities]
  (actions.activity/remote-create activities))

;; TODO: fetch all in 1 request
(defn fetch-comments
  [activity]
  (let [comments (concat (map model.activity/fetch-by-id (:comments activity))
                         (if-let [irt (first (:irts activity))]
                           (model.activity/fetch-all {:id irt})))]
    [activity comments]))

(defn comment-request
  [activity]
  (tigase/make-packet
   {:type :get
    :from (tigase/make-jid "" (config :domain))
    :to (tigase/make-jid (actions.activity/get-author activity))
    :body
    (element/make-element
     ["pubsub" {"xmlns" ns/pubsub}
      ["items" {"node" (comment-node-uri activity)}]])}))

(defn fetch-comments-onesocialweb
  [activity]
  (model/implement))

;; This should be a trigger
(defaction fetch-comments-remote
  [activity]
  (let [author (actions.activity/get-author activity)
        domain (model.user/get-domain author)]
    (when (:xmpp domain)
      (tigase/deliver-packet! (comment-request activity)))))

(definitializer
  (doseq [namespace ['jiksnu.filters.comment-filters
                     'jiksnu.views.comment-views]]
    (require namespace)))
