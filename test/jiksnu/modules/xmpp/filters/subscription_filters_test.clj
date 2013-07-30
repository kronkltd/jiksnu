(ns jiksnu.modules.xmpp.filters.subscription-filters-test
  (:use [ciste.core :only [with-serialization]]
        [ciste.filters :only [filter-action]]
        [jiksnu.test-helper :only [context future-context test-environment-fixture]]
        jiksnu.actions.subscription-actions
        [midje.sweet :only [=> throws truthy]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.model :as model]
            [jiksnu.session :as session]
            [jiksnu.actions.user-actions :as actions.user]
            jiksnu.modules.xmpp.filters.subscription-filters
            [jiksnu.model.subscription :as model.subscription]
            [jiksnu.model.user :as model.user]
            [jiksnu.util :as util]))

(test-environment-fixture

 (context #'filter-action
   (context #'get-subscribers
     (context "when the serialization is :xmpp"
       (with-serialization :xmpp

         (context "when a user matching the to param is found"
           (filter-action
            #'get-subscribers
            {:to .jid.}) => truthy
            (provided
              (model.user/fetch-by-jid .jid.) => .user.
              (get-subscribers .user.) => true))

         (context "when a user matching the to param is not found"
           (filter-action
            #'get-subscribers
            {:to .jid.}) => nil
            (provided
              (model.user/fetch-by-jid .jid.) => nil
              (get-subscribers .user.) => nil :times 0))
         ))
     )
   )

 (context #'filter-action
   (context #'get-subscriptions
     (context "when the serialization is :xmpp"
       (let [action #'get-subscriptions]
         (with-serialization :xmpp

           (context "when a user matching the to param is found"
             (let [request {:to .jid.}]
               (filter-action action request) => truthy
               (provided
                 (model.user/fetch-by-jid .jid.) => .user.
                 (get-subscriptions .user.) => true)))

            (context "when a user matching the to param is not found"
              (let [request {:to .jid.}]
                (filter-action action request) => nil
                (provided
                  (model.user/fetch-by-jid .jid.) => nil
                  (get-subscriptions .user.) => nil :times 0)))
            )))
     )
   )
 )
