(ns jiksnu.views.subscription-views-test
  (:use ciste.core
        ciste.debug
        ciste.factory
        ciste.views
        clj-tigase.core
        jiksnu.core-test
        jiksnu.helpers.subscription-helpers
        jiksnu.namespace
        jiksnu.sections.subscription-sections
        jiksnu.view
        jiksnu.views.subscription-views
        jiksnu.xmpp.element
        [lazytest.describe :only (describe do-it testing)]
        [lazytest.expect :only (expect)])
  (:require [jiksnu.model.subscription :as model.subscription]
            [jiksnu.model.user :as model.user]
            [jiksnu.actions.subscription-actions :as
             actions.subscription])
  (:import jiksnu.model.User))

(describe apply-view "#'subscribe :xmpp")

(describe apply-view  "#'unsubscribe :xmpp"
  (testing "when there is no subscription"
    (do-it "should return a packet map"
      (let [user (model.user/create (factory User))
            subscribee (model.user/create (factory User))
            element (make-element
                     ["pubsub" {"xmlns" pubsub-uri}
                      ["unsubscribe" {"node" microblog-uri}]])
            packet (make-packet
                    {:to (make-jid subscribee)
                     :from (make-jid user)
                     :type :set
                     :body element})
            request (merge (make-request packet)
                           {:action #'actions.subscription/unsubscribe
                            :format :xmpp})
            record (actions.subscription/unsubscribe (:_id user)
                                                     (:_id subscribee))
            response (apply-view request record)]
        (expect (map? response))))))

(describe apply-view "#'subscribed :xmpp")

(describe apply-view "#'remote-subscribe-confirm :xmpp")
