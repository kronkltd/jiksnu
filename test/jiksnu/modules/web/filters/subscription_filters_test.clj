(ns jiksnu.modules.web.filters.subscription-filters-test
  (:require [ciste.core :refer [with-serialization]]
            [ciste.filters :refer [filter-action]]
            [clj-factory.core :refer [fseq]]
            [clojure.tools.logging :as log]
            [jiksnu.model :as model]
            [jiksnu.session :as session]
            [jiksnu.actions.subscription-actions :as actions.subscription]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.mock :as mock]
            [jiksnu.model.subscription :as model.subscription]
            [jiksnu.model.user :as model.user]
            [jiksnu.test-helper :refer [check context future-context test-environment-fixture]]
            [jiksnu.util :as util]
            [midje.sweet :refer [=> throws truthy]]))

(test-environment-fixture

 (context #'filter-action

   (context #'actions.subscription/delete
     (let [action #'actions.subscription/delete]
       (context "when the serialization is :http"
         (with-serialization :http
           (context "when a subscription matching the id param is found"
             (let [request {:params {:id .id.}}]
               (filter-action action request)) => truthy
               (provided
                 (model.subscription/fetch-by-id .id.) => .subscription.
                 (delete .subscription.) => true))
           (context "when a subscription matching the id param is not found"
             (let [request {:params {:id .id.}}]
               (filter-action actions request)) => nil
               (provided
                 (model.subscription/fetch-by-id .id.) => nil
                 (delete .subscription.) => true :times 0))))))

   (context #'actions.subscription/ostatussub-submit
     (with-serialization :http
       (let [action #'actions.subscription/ostatussub-submit]
        (context "when given a url in the form of 'user@domain'"
          (let [actor (mock/a-user-exists)
                username (fseq :username)
                domain-name (fseq :domain)
                uri (format "%s@%s" username domain-name)
                request {:params {:profile uri}}]
            (session/with-user actor
              (filter-action action request)) =>
            (check [response]
              response => map?))))))

   (context #'actions.subscription/subscribe
     (let [action #'actions.subscription/subscribe]
       (context "when the serialization is :http"
         (with-serialization :http
           (let [request {:params {:subscribeto .id.}}]

             (context "when a user matching the subscribeto param is found"

               (context "when the user is authenticated"
                 (filter-action action request) => truthy
                 (provided
                   (model.user/fetch-by-id .id.) => .target.
                   (session/current-user) => .actor.
                   (subscribe .actor. .target.) => true))

               (context "when the user is not authenticated"
                 (filter-action action request) => (throws RuntimeException)
                 (provided
                   (model.user/fetch-by-id .id.) => nil
                   (session/current-user) => .actor. :times 0
                   (subscribe .actor. .target.) => nil :times 0))
               )

             (context "when a user matching the subscribeto param is not found"
               (filter-action action request) => (throws RuntimeException)
               (provided
                 (session/current-user) => .actor. :times 0
                 (model.user/fetch-by-id .id.) => nil
                 (subscribe .actor. .target.) => nil :times 0))
             )))
       ))
   ))
