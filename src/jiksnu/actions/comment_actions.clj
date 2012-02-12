(ns jiksnu.actions.comment-actions
  (:use (ciste [config :only [definitializer]]
               [core :only [defaction]]))
  (:require (clj-tigase [core :as tigase])
            (jiksnu.actions [activity-actions :as actions.activity])
            (jiksnu.model [activity :as model.activity]
                          [domain :as model.domain]
                          [user :as model.user])))

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
(defaction fetch-comments
  [activity]
  [activity
   (map model.activity/show (:comments activity))])

(defn comment-request
  [activity]
  (tigase/make-packet
   {:type :get
    :from (tigase/make-jid "" (config :domain))
    :to (tigase/make-jid (get-author activity))
    :body
    (element/make-element
     ["pubsub" {"xmlns" namespace/pubsub}
      ["items" {"node" (comment-node-uri activity)}]])}))

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
