(ns jiksnu.xmpp.view.user-view-test
  (:use ciste.core
        ciste.factory
        ciste.view
        jiksnu.model
        [jiksnu.xmpp.view :only (make-request element? make-packet make-jid)]
        jiksnu.xmpp.view.user-view
        jiksnu.view
        [lazytest.describe :only (describe testing do-it)]
        [lazytest.expect :only (expect)])
  (:require [jiksnu.xmpp.controller.user-controller :as controller.user]
            [jiksnu.model.user :as model.user])
  (:import jiksnu.model.User))

(describe show-section "User :xmpp :xmpp"
  (do-it "should return an element"
    (with-serialization :xmpp
      (with-format :xmpp
        (let [user (model.user/create (factory User))]
          (let [response (show-section user)]
            (expect (element? response))))))))

(describe apply-view "#'show :xmpp"
  (do-it "should return a query results packet map"
    (with-format :xmpp
      (with-serialization :xmpp
        (let [user (model.user/create (factory User))
              packet (make-packet
                      {:to (make-jid user)
                       :from (make-jid user)
                       :type :get})
              request (merge {:format :xmpp
                              :action #'controller.user/show}
                             (make-request packet))]
          (let [response (apply-view request user)]
            (expect (map? response))
            (expect (= :result (:type response)))))))))

(describe apply-view "#'fetch-remote :xmpp"
  (do-it "should return an iq query packet map"
    (with-format :xmpp
      (with-serialization :xmpp
        (let [user (model.user/create (factory User))
              packet (make-packet
                      {:to (make-jid user)
                       :from (make-jid user)
                       :type :get})
              request (merge {:format :xmpp
                              :action #'controller.user/fetch-remote}
                             (make-request packet))]
          (let [response (apply-view request user)]
            (expect (map? response))
            (expect (= :get (:type response)))))))))

(describe apply-view "#'remote-create :xmpp"
  (do-it "should return a query results packet map"
    (with-format :xmpp
      (with-serialization :xmpp
        (let [user (model.user/create (factory User))
              packet (make-packet
                      {:to (make-jid user)
                       :from (make-jid user)
                       :type :get})
              request (merge {:format :xmpp
                              :action #'controller.user/remote-create}
                             (make-request packet))]
          (let [response (apply-view request user)]
            (expect (map? response))
            (expect (= :result (:type response)))))))))
