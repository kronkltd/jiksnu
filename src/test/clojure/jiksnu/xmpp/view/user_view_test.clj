(ns jiksnu.xmpp.view.user-view-test
  (:use ciste.core
        jiksnu.factory
        jiksnu.mock
        jiksnu.model
        [jiksnu.xmpp.view :only (make-request element? make-packet make-jid)]
        jiksnu.xmpp.view.user-view
        jiksnu.view
        [lazytest.describe :only (describe testing do-it)]
        [lazytest.expect :only (expect)])
  (:require [jiksnu.xmpp.controller.user-controller :as controller.user]
            [jiksnu.model.user :as model.user])
  (:import jiksnu.model.User))

(describe display-minimal "Statement")

(describe apply-view "#'show :xmpp")

;; (describe apply-view "#'index :xmpp"
;;   (do-it "should contain a vcard query response element"
;;     (let [user (model.user/create (factory User))
;;           element (mock-vcard-query-request-element)
;;           packet (make-packet
;;                   {:from (make-jid user)
;;                    :to (make-jid user)
;;                    :type :get
;;                    :body element})
;;           request (make-request packet)
;;           record (controller.user/index request)
;;           response (apply-view request record)]
;;       (expect (element? response)))))
