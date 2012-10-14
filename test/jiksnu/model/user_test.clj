(ns jiksnu.model.user-test
  (:use [ciste.config :only [config with-environment]]
        [clj-factory.core :only [factory fseq]]
        jiksnu.test-helper
        jiksnu.model
        jiksnu.model.user
        midje.sweet)
  (:require [clj-tigase.packet :as packet]
            [clojure.tools.logging :as log]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.model.domain :as model.domain])
  (:import jiksnu.model.Domain
           jiksnu.model.User))

(test-environment-fixture

 (def domain-a (actions.domain/find-or-create (factory :domain)))
 (def user-a (create (factory :user {:domain (:_id domain-a)})))

 ;; (fact "#'salmon-link"
 ;;   (salmon-link .user.) => string?
 ;;   (provided
 ;;     (:domain .user.) => (config :domain)
 ;;     (:_id .user.) => (make-id)))
 
 (fact "#'get-domain"
   (fact "when passed nil"
     (fact "should throw an exception"
       (get-domain nil) => (throws Exception)))
   (fact "when passed a user"
     (fact "should return that user's domain"
       (get-domain user-a) => domain-a)))

 (fact "#'local?"
   (fact "when passed a user user"
     (fact "and it's domain is the same as the current domain"
       (fact "should be true"
         (let [domain (actions.domain/current-domain)
               user (factory :local-user)]
           (local? user) => truthy)))
     (fact "and it's domain is different from the current domain"
       (fact "should be false"
         (let [domain (model.domain/create (factory :domain))
               user (factory :user {:domain (:_id domain)})]
           (local? user) => falsey)))))

 (fact "split-uri"
   (split-uri "bob@example.com") => ["bob" "example.com"]
   (split-uri "acct:bob@example.com") => ["bob" "example.com"]
   (split-uri "http://example.com/bob") => nil)

 (fact "display-name"
   (display-name .user.) => string?)

 (fact "get-link"
   (let [user (factory :user {:links [{:rel "foo" :href "bar"}]})]
     (get-link user "foo" nil) => (contains {:href "bar"})
     (get-link user "baz" nil) => nil))

 (fact "#'create"
   (create (factory :local-user)) => (partial instance? User))

 ;; TODO: This is a better test for actions
 (fact "#'fetch-all"
   (fact "when there are no users"
     (fact "should be empty"
       ;; TODO: all collections should be emptied in background
       (drop!)
       (fetch-all) =>
       (every-checker
        empty?)))

   (fact "when there are users"
     (fact "should not be empty"
       (create (factory :user))
       (fetch-all) =>
       (every-checker
        seq?
        (partial every? user?)))))

 (fact "#'fetch-by-domain"
   (let [domain (model.domain/create (factory :domain))
         user (create (factory :user {:domain (:_id domain)}))]
     (fetch-by-domain domain) => (contains user)))
 
 (fact "#'get-user"
   (fact "when the user is found"
     (let [username (fseq :username)
           domain (actions.domain/find-or-create (factory :domain))]
       (create (factory :user {:username username
                              :domain (:_id domain)}))
       (get-user username (:_id domain)) => user?))

   (fact "when the user is not found"
     (drop!)
     (let [username (fseq :id)
           domain (actions.domain/find-or-create (factory :domain))]
       (get-user username (:_id domain)) => nil)))

 (fact "user-meta-uri"
   (fact "when the user's domain does not have a lrdd link"
     (model.domain/drop!)
     (let [user (create (factory :user))]
       (user-meta-uri user) => (throws RuntimeException)))

   (fact "when the user's domain has a lrdd link"
     (let [domain (actions.domain/find-or-create
                   (factory :domain
                            {:links [{:rel "lrdd"
                                      :template "http://example.com/main/xrd?uri={uri}"}]}))
           user (create (factory :user {:domain (:_id domain)}))]
       (user-meta-uri user) => (str "http://example.com/main/xrd?uri=" (get-uri user)))))

 (fact "vcard-request"
   (let [user (create (factory :user))]
     (vcard-request user) => packet/packet?))
)
