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

(defsection show-section-minimal [User :xmpp :xmpp]
  [property & options]
  (make-element
   (:key property) {}
   [(make-element (:type property) {}
                  [(:value property)])]))

(defsection show-section [User :xmpp :xmpp]
  [^User user & options]
  (make-element
   "vcard" {"xmlns" vcard-uri}
   [(if (:name user)
      (make-element
       "fn" {}
       [(make-element "text" {}
                      [(:name user)])]))
    (if (:avatar-url user)
      (make-element
       "photo" {}
       [(make-element "uri" {}
                      [(:avatar-url user)])]))]))

(defview #'show :xmpp
  [request user]
  {:body
   (make-element
    "iq" {"type" "result"
          "id" (:id request)}
    [(make-element
      "query" {"xmlns" query-uri}
      [(show-section user)])])
   :from (:to request)
   :to (:from request)})

;; (defview #'index :xmpp
;;   [request statements]
;;   {:body
;;    (make-element
;;     "query" {"xmlns" query-uri}
;;     [(make-element
;;       "vcard" {"xmlns" vcard-uri}
;;       (map show-section-minimal statements))])})

(defview #'inbox :xmpp
  [activities]
  {:body (index-section activities)})
