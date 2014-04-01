(ns jiksnu.modules.xmpp.views.comment-views
  (:require [ciste.views :refer [defview]]
            [ciste.sections.default :refer [index-section]]
            [clj-tigase.core :as tigase]
            [clj-tigase.element :as element]
            [clj-tigase.packet :as packet]
            [jiksnu.actions.comment-actions :as actions.comment]
            [jiksnu.namespace :as ns]
            [jiksnu.routes.helpers :refer [named-path]]
            [ring.util.response :as response]))

(defview #'actions.comment/comment-response :xmpp
  [request activity])

(defview #'actions.comment/fetch-comments :xmpp
  [request [activity comments]]
  (tigase/result-packet request (index-section comments)))

;; fetch-comments-remote

(defview #'actions.comment/fetch-comments-remote :xmpp
  [request activity]
  {:type :get
   :body
   (element/make-element
    (packet/pubsub-items (str ns/microblog ":replies:item=" (:id activity))))})
