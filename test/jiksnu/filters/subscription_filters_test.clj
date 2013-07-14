(ns jiksnu.filters.subscription-filters-test
  (:use [ciste.core :only [with-serialization]]
        [ciste.filters :only [filter-action]]
        [jiksnu.test-helper :only [context future-context test-environment-fixture]]
        jiksnu.actions.subscription-actions
        [midje.sweet :only [=> throws truthy]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.model :as model]
            [jiksnu.session :as session]
            [jiksnu.actions.user-actions :as actions.user]
            jiksnu.filters.subscription-filters
            [jiksnu.model.subscription :as model.subscription]
            [jiksnu.model.user :as model.user]
            [jiksnu.util :as util]))

(test-environment-fixture

 (context #'filter-action
   (context #'delete
     (context "when the serialization is :http"
       (with-serialization :http
         (context "when a subscription matching the id param is found"
           (filter-action
            #'delete
            {:params {:id .id.}}) => truthy
            (provided
              (model.subscription/fetch-by-id .id.) => .subscription.
              (delete .subscription.) => true))
         (context "when a subscription matching the id param is not found"
           (filter-action
            #'delete
            {:params {:id .id.}}) => nil
            (provided
              (model.subscription/fetch-by-id .id.) => nil
              (delete .subscription.) => true :times 0)))))

   (context #'subscribe
     (let [action #'subscribe]
       (context "when the serialization is :http"
         (with-serialization :http
           (context "when a user matching the subscribeto param is found"
             (context "when the user is authenticated"
               (let [request {:params {:subscribeto .id.}}]
                 (filter-action action request) => truthy
                 (provided
                   (model.user/fetch-by-id (util/make-id .id.)) => .target.
                   (session/current-user) => .actor.
                   (subscribe .actor. .target.) => true)))
             (context "when the user is not authenticated"
               (let [request {:params {:subscribeto .id.}}]
                 (filter-action action request) => (throws RuntimeException)
                 (provided
                   (model.user/fetch-by-id (util/make-id .id.)) => nil
                   (session/current-user) => .actor. :times 0
                   (subscribe .actor. .target.) => nil :times 0))))
           (context "when a user matching the subscribeto param is not found"
             (let [request {:params {:subscribeto .id.}}]
               (filter-action action request) => (throws RuntimeException)
               (provided
                 (session/current-user) => .actor. :times 0
                 (model.user/fetch-by-id (util/make-id .id.)) => nil
                 (subscribe .actor. .target.) => nil :times 0)))))))

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
              (get-subscribers .user.) => nil :times 0)))))

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
                 (get-subscriptions .user.) => nil :times 0)))))))))
