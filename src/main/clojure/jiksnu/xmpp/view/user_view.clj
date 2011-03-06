(ns jiksnu.xmpp.view.user-view
  (:use ciste.core
        ciste.view
        jiksnu.model
        jiksnu.namespace
        jiksnu.view
        jiksnu.xmpp.controller.user-controller
        jiksnu.xmpp.view)
  (:require [jiksnu.model.user :as model.user])
  (:import jiksnu.model.User
           tigase.xml.Element))

;; (defsection show-section-minimal [User :xmpp :xmpp]
;;   [property & options]
;;   (make-element
;;    (:key property) {}
;;    [(:type property) {} (:value property)]))

(defsection show-section [User :xmpp :xmpp]
  [^User user & options]
  (let [{:keys [name avatar-url]} user]
    (make-element
     "vcard" {"xmlns" vcard-uri}
     (if name
       ["fn" {}
        ["text" {} name]])
     (if avatar-url
       ["photo" {}
        ["uri" {} avatar-url]]))))

(defview #'show :xmpp
  [request user]
  (let [{:keys [id to from]} request]
    {:body
     (make-element
      "query" {"xmlns" query-uri} (show-section user))
     :type :result
     :id id
     :from to
     :to from}))

(defview #'fetch-remote :xmpp
  [request user]
  (let [{:keys [to from]} request]
    {:from to
     :to from
     :type :get}))

(defview #'remote-create :xmpp
  [request user]
  (let [{:keys [to from]} request]
    {:from to
     :to from
     :type :result})
  )
