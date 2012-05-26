(ns jiksnu.views.pubsub-views
  (:use [ciste.config :only [config]]
        [ciste.debug :only [spy]]
        ciste.sections
        [ciste.views :only [defview]]
        ciste.sections.default
        jiksnu.actions.pubsub-actions
        jiksnu.model
        jiksnu.session)
  (:require [jiksnu.model.user :as model.user]))

;; (defview #'callback :html
;;   [request params]
;;   {:body params
;;    :template false})

;; (defview #'admin-index :html
;;   [request subscriptions]
;;   {:body (index-section subscriptions)})

(defview #'hub-dispatch :html
  [request _]
  {:template :false})

;; (defview #'hub-publish :html
;;   [request response]
;;   (merge {:template :false}
;;          response))

;; (defview #'subscribe :html
;;   [request _]
;;   {:template :false})
