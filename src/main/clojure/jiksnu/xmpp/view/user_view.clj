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

(defmethod show-section-minimal User
  [property]
  (make-element
   (:key property) {}
   [(make-element (:type property) {}
                  [(:value property)])]))

(defmethod show-section User
  [^User user]
  (make-element
   "vcard" {"xmlns" vcard-uri}
   [(make-element
     "fn" {}
     [(make-element "text" {} ["Daniel E. Renfer"])])]))

(defview #'show :xmpp
  [request user]
  {:body
   (make-element
    "iq" {"type" "result"}
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
