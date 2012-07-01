(ns jiksnu.filters.subscription-filters-test
  (:use [ciste.core :only [with-serialization]]
        [ciste.filters :only [filter-action]]
        [jiksnu.test-helper :only [test-environment-fixture]]
        jiksnu.actions.subscription-actions
        midje.sweet)
  (:require [clojure.tools.logging :as log]
            [jiksnu.model :as model]
            [jiksnu.session :as session]
            [jiksnu.actions.user-actions :as actions.user]
            jiksnu.filters.subscription-filters
            [jiksnu.model.subscription :as model.subscription]
            [jiksnu.model.user :as model.user]))

(test-environment-fixture

 (fact "#'filter-action [#'delete :http]"
   (with-serialization :http
     (fact "when a subscription matching the id param is found"
       (filter-action
        #'delete
        {:params {:id .id.}}) => truthy
      (provided
        (model.subscription/fetch-by-id .id.) => .subscription.
        (delete .subscription.) => truthy))
     (fact "when a subscription matching the id param is not found"
       (filter-action
        #'delete
        {:params {:id .id.}}) => nil
         (provided
           (model.subscription/fetch-by-id .id.) => nil
           (delete .subscription.) => truthy :times 0))))

 (fact "filter-action #'subscribe :http"
   (let [action #'subscribe]
     (fact "when the serialization is :http"
      (with-serialization :http
        (fact "when a user matching the subscribeto param is found"
          (fact "when the user is authenticated"
            (let [request {:params {:subscribeto .id.}}]
              (filter-action action request) => truthy
              (provided
                (model.user/fetch-by-id (model/make-id .id.)) => .target.
                (session/current-user) => .actor.
                (subscribe .actor. .target.) => truthy)))
          (fact "when the user is not authenticated"
            (let [request {:params {:subscribeto .id.}}]
              (filter-action action request) => (throws RuntimeException)
              (provided
                (model.user/fetch-by-id (model/make-id .id.)) => nil
                (session/current-user) => .actor. :times 0
                (subscribe .actor. .target.) => nil :times 0))))
        (fact "when a user matching the subscribeto param is not found"
          (let [request {:params {:subscribeto .id.}}]
            (filter-action action request) => (throws RuntimeException)
            (provided
              (session/current-user) => .actor. :times 0
              (model.user/fetch-by-id (model/make-id .id.)) => nil
              (subscribe .actor. .target.) => nil :times 0)))))))

   (fact "#'filter-action [#'get-subscribers :xmpp]"
     (with-serialization :xmpp
       (fact "when a user matching the to param is found"
        (filter-action
         #'get-subscribers
         {:to .jid.}) => truthy
        (provided
          (actions.user/fetch-by-jid .jid.) => .user.
          (get-subscribers .user.) => truthy))
       (fact "when a user matching the to param is not found"
         (filter-action
          #'get-subscribers
          {:to .jid.}) => nil
          (provided
            (actions.user/fetch-by-jid .jid.) => nil
            (get-subscribers .user.) => nil :times 0))))

   (fact "#'filter-action [#'get-subscriptions :xmpp]"
     (let [action #'get-subscriptions]
       (with-serialization :xmpp
         (fact "when a user matching the to param is found"
           (let [request {:to .jid.}]
             (filter-action action request) => truthy
             (provided
               (actions.user/fetch-by-jid .jid.) => .user.
               (get-subscriptions .user.) => truthy)))
         (fact "when a user matching the to param is not found"
           (let [request {:to .jid.}]
             (filter-action action request) => nil
             (provided
               (actions.user/fetch-by-jid .jid.) => nil
               (get-subscriptions .user.) => nil :times 0)))))))
