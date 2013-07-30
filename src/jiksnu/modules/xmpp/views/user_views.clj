(ns jiksnu.modules.xmpp.views.user-views
  (:use [ciste.views :only [defview]]
        [ciste.sections.default :only [show-section]]
        [jiksnu.actions.user-actions :only [fetch-remote show remote-create
                                            xmpp-service-unavailable]])
  (:require [clj-tigase.element :as element]
            [jiksnu.namespace :as ns]
            [jiksnu.model.user :as model.user]))

;; fetch-remote

(defview #'fetch-remote :xmpp
  [request user]
  (model.user/vcard-request user))

(defview #'show :xmpp
  [request user]
  (let [{:keys [id to from]} request]
    {:body (element/make-element
            "query" {"xmlns" ns/vcard-query}
            (show-section user))
     :type :result
     :id id
     :from to
     :to from}))

;; (defview #'remote-create :xmpp
;;   [request user]
;;   (let [{:keys [to from]} request]
;;     {:from to
;;      :to from
;;      :type :result}))

(defview #'xmpp-service-unavailable :xmpp
  [request _])

