(ns jiksnu.modules.web.views.pubsub-views
  (:require [ciste.config :refer [config]]
            [ciste.views :refer [defview]]
            [jiksnu.actions.pubsub-actions :refer [hub-dispatch]]
            [jiksnu.model.user :as model.user]))

;; (defview #'callback :html
;;   [request params]
;;   {:body params
;;    :template false})

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
