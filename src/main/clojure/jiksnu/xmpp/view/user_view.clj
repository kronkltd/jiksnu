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
      "iq" {"type" "result"
            "id" id}
      ["query" {"xmlns" query-uri} (show-section user)])
     :from to
     :to from}))

;; (defview #'index :xmpp
;;   [request statements]
;;   {:body
;;    (make-element
;;     "query" {"xmlns" query-uri}
;;     [(make-element
;;       "vcard" {"xmlns" vcard-uri}
;;       (map show-section-minimal statements))])})
