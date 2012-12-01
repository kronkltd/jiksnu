(ns jiksnu.model.user-test
  (:use [ciste.config :only [config with-environment]]
        [clj-factory.core :only [factory fseq]]
        [jiksnu.test-helper :only [test-environment-fixture]]
        jiksnu.model.user
        [midje.sweet :only [=> contains every-checker fact throws truthy falsey]])
  (:require [clj-tigase.packet :as packet]
            [clojure.tools.logging :as log]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.existance-helpers :as existance]
            [jiksnu.features-helper :as feature]
            [jiksnu.model :as model]
            [jiksnu.model.domain :as model.domain])
  (:import jiksnu.model.Domain
           jiksnu.model.User))

(test-environment-fixture

 (def domain-a (actions.domain/current-domain))
 (def user-a (existance/a-user-exists))

 (fact "#'get-domain-name"
   (fact "when given a http uri"
     (get-domain-name "http://example.com/users/1") => "example.com")

   (fact "when given an acct uri"
     (get-domain-name "acct:bob@example.com") => "example.com"))

 (fact "#'get-domain"
   (fact "when passed nil"
     (fact "should throw an exception"
       (get-domain nil) => (throws Exception)))
   (fact "when passed a user"
     (fact "should return that user's domain"
       (get-domain user-a) => domain-a)))

 (fact "#'local?"
   (fact "when passed a user"
     (fact "and it's domain is the same as the current domain"
       (let [user (existance/a-user-exists)]
         (local? user) => true))
     (fact "and it's domain is different from the current domain"
       (let [user (existance/a-remote-user-exists)]
         (local? user) => false))))

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

 (fact "#'fetch-by-id"
   (fact "when the item doesn't exist"
     (let [id (model/make-id)]
       (fetch-by-id id) => nil?))

   (fact "when the item exists"
     (let [item (existance/a-user-exists)]
      (fetch-by-id (:_id item)) => item)))

 (fact "#'create"
   (fact "when given valid params"
     (let [params (actions.user/prepare-create
                   (factory :local-user))]
       (create params) => (partial instance? User)))

   (fact "when given invalid params"
     (create {}) => (throws RuntimeException)))

 (fact "#'drop!"
   (dotimes [i 1]
     (existance/a-user-exists))
   (drop!)
   (count-records) => 0)

 (fact "#'delete"
   (let [item (existance/a-user-exists)]
     (delete item) => item
     (fetch-by-id (:_id item)) => nil))

 (fact "#'fetch-all"
   (fact "when there are no items"
     (drop!)
     (fetch-all) => (every-checker
      seq?
      empty?))

   (fact "when there is more than a page of items"
     (drop!)

     (let [n 25]
       (dotimes [i n]
         (existance/a-user-exists))

       (fetch-all) =>
       (every-checker
        seq?
        #(fact (count %) => 20))

       (fetch-all {} {:page 2}) =>
       (every-checker
        seq?
        #(fact (count %) => (- n 20))))))

 (fact "#'count-records"
   (fact "when there aren't any items"
     (drop!)
     (count-records) => 0)
   (fact "when there are items"
     (drop!)
     (let [n 15]
       (dotimes [i n]
         (existance/a-user-exists))
       (count-records) => n)))

 (fact "#'fetch-by-domain"
   (let [domain (actions.domain/current-domain)
         user (existance/a-user-exists)]
     (fetch-by-domain domain) => (contains user)))

 (fact "#'get-user"
   (fact "when the user is found"
     (let [user (existance/a-user-exists)
           username (:username user)
           domain (actions.user/get-domain user)]
       (get-user username (:_id domain)) => user))

   (fact "when the user is not found"
     (drop!)
     (let [username (fseq :id)
           domain (existance/a-domain-exists)]
       (get-user username (:_id domain)) => nil)))

 (fact "user-meta-uri"
   (fact "when the user's domain does not have a lrdd link"
     (model.domain/drop!)
     (let [user (existance/a-user-exists)]
       (user-meta-uri user) => (throws RuntimeException)))

   (fact "when the user's domain has a lrdd link"
     (let [domain (existance/a-remote-domain-exists)]
       (model.domain/set-field! domain :links [{:rel "lrdd"
                                               :template "http://example.com/main/xrd?uri={uri}"}])
       (let [user (existance/a-remote-user-exists {:domain domain})]
         (user-meta-uri user) => (str "http://example.com/main/xrd?uri=" (get-uri user))))))

 (fact "vcard-request"
   (let [user (existance/a-user-exists)]
     (vcard-request user) => packet/packet?))
 )
