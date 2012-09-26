(ns jiksnu.helpers.user-helpers-test
  (:use [ciste.config :only [with-environment]]
        clj-factory.core
        midje.sweet
        jiksnu.test-helper
        jiksnu.model
        jiksnu.helpers.user-helpers)
  (:require [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.model.domain :as model.domain]
            [jiksnu.model.user :as model.user]
            [jiksnu.model.webfinger :as model.webfinger])
  (:import jiksnu.model.Domain
           jiksnu.model.User))

(test-environment-fixture

 
 (future-fact "#'fetch-user-meta"
   (fact "when the user has a user meta link"
    (fact "when the user meta can be found"
      (fact "should return a xml stream"
        (fetch-user-meta .user.) => truthy
        (provided
          (model.user/user-meta-uri .user.) => .url.
          (model.webfinger/fetch-host-meta .url.) => truthy)))
    (fact "when the user meta can not be found"
      (fact "should return an xml stream"
        (fetch-user-meta .user.) => nil
        (provided
          (model.user/user-meta-uri .user.) => .url.
          (model.webfinger/fetch-host-meta .url.) => nil))))
   (fact "when the user does not have a user meta link"
     (fact "should throw an exception"
       (
        fetch-user-meta .user.) => (throws RuntimeException)
       (provided
         (model.user/user-meta-uri .user.) => nil)))))

