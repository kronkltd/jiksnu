(ns jiksnu.views.comment-views
  (:use (ciste [views :only (defview)])
        ciste.sections.default
        jiksnu.actions.comment-actions
        jiksnu.views.comment-views)
  (:require (clj-tigase [core :as tigase]
                        [element :as element]
                        [packet :as packet])
            (jiksnu [namespace :as namespace])
            (ring.util [response :as response])))

(defview #'add-comment :html
  [request activity]
  (-> (response/redirect-after-post "/")
      (assoc :template false)))

(defview #'comment-response :html
  [request activity]
  (-> (response/redirect-after-post "/")
      (assoc :template false)))

(defview #'comment-response :xmpp
  [request activity])

(defview #'fetch-comments :html
  [request [activity comments]]
  (-> (response/redirect-after-post (uri activity))
      (assoc :template false)
      (assoc :flash "comments are being fetched")))

(defview #'fetch-comments :xmpp
  [request [activity comments]]
  (tigase/result-packet request (index-section comments)))

(defview #'fetch-comments-remote :xmpp
  [request activity]
  {:type :get
   :body
   (element/make-element (packet/pubsub-items
     (str namespace/microblog ":replies:item=" (:id activity))))})

