(ns jiksnu.views.pubsub-views
  (:use [ciste.config :only [config]]
        [ciste.views :only [defview]]
        [jiksnu.actions.pubsub-actions :only [hub-dispatch]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.model.user :as model.user]))

;; (defview #'callback :html
;;   [request params]
;;   {:body params
;;    :template false})

;; (defview #'admin-index :html
;;   [request subscriptions]
;;   {:body (index-section subscriptions)})

(defview #'hub-dispatch :html
  [request response]
  {:template false
   :status 204})

;; (defview #'hub-publish :html
;;   [request response]
;;   (merge {:template :false}
;;          response))

;; (defview #'subscribe :html
;;   [request _]
;;   {:template :false})
