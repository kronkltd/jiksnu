(ns jiksnu.xmpp.view.activity-view
  (:use clj-tigase.core
        ciste.core
        ciste.trigger
        ciste.sections
        ciste.view
        [ciste.config :only (config)]
        jiksnu.model
        jiksnu.namespace
        jiksnu.session
        jiksnu.xmpp.controller.activity-controller
        jiksnu.xmpp.element
        jiksnu.xmpp.view
        jiksnu.view)
  (:require [jiksnu.atom.view.activity-view :as atom.view.activity]
            [jiksnu.model.activity :as activity.model]
            [jiksnu.model.item :as model.item]
            [jiksnu.model.subscription :as model.subscription]
            [jiksnu.model.user :as model.user]
            jiksnu.http.controller.activity-controller)
  (:import jiksnu.model.Activity))

(defview #'index :xmpp
  [request activities]
  (result-packet request (index-section activities)))

(defview #'remote-create :xmpp
  [request _]
  nil)

(defview #'fetch-comments :xmpp
  [request activities]
  (result-packet request (index-section activities)))

(defview #'fetch-comments-remote :xmpp
  [request activity]
  {:type :get
   :body
   (make-element (pubsub-items
     (str microblog-uri ":replies:item=" (:id activity))))})
