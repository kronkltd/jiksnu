(ns jiksnu.views.comment-views
  (:use [ciste.views :only [defview]]
        ciste.sections.default
        [clojurewerkz.route-one.core :only [named-path]]
        jiksnu.actions.comment-actions)
  (:require [clj-tigase.core :as tigase]
            [clj-tigase.element :as element]
            [clj-tigase.packet :as packet]
            [jiksnu.namespace :as ns]
            [ring.util.response :as response]))

(defview #'comment-response :xmpp
  [request activity])

(defview #'fetch-comments :xmpp
  [request [activity comments]]
  (tigase/result-packet request (index-section comments)))

;; fetch-comments-remote

(defview #'fetch-comments-remote :xmpp
  [request activity]
  {:type :get
   :body
   (element/make-element
    (packet/pubsub-items (str ns/microblog ":replies:item=" (:id activity))))})
