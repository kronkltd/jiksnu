(ns jiksnu.model.user-test
  (:use [clj-factory.core :only [factory fseq]]
        [jiksnu.test-helper :only [check context future-context test-environment-fixture]]
        jiksnu.model.user
        [midje.sweet :only [=> contains throws falsey]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.mock :as mock]
            [jiksnu.model :as model]
            [jiksnu.model.domain :as model.domain]
            [jiksnu.util :as util])
  (:import jiksnu.model.Domain
           jiksnu.model.User))

(test-environment-fixture

 (def domain-a (actions.domain/current-domain))
 (def user-a (mock/a-user-exists))

 (context #'count-records
   (context "when there aren't any items"
     (drop!)
     (count-records) => 0)
   (context "when there are items"
     (drop!)
     (let [n 15]
       (dotimes [i n]
         (mock/a-user-exists))
       (count-records) => n)))

 (context #'delete
   (let [item (mock/a-user-exists)]
     (delete item) => item
     (fetch-by-id (:_id item)) => nil))

 (context #'drop!
   (dotimes [i 1]
     (mock/a-user-exists))
   (drop!)
   (count-records) => 0)

 (context #'fetch-by-id
   (context "when the item doesn't exist"
     (let [id "acct:foo@bar.baz"]
       (fetch-by-id id) => nil?))

   (context "when the item exists"
     (let [item (mock/a-user-exists)]
       (fetch-by-id (:_id item)) => item)))

 (context #'create
   (context "when given valid params"
     (let [params (actions.user/prepare-create
                   (factory :local-user))]
       (create params) => (partial instance? User)))

   (context "when given invalid params"
     (create {}) => (throws RuntimeException)))

 (context #'fetch-all
   (context "when there are no items"
     (drop!)
     (fetch-all) => empty?)

   (context "when there is more than a page of items"
     (drop!)

     (let [n 25]
       (dotimes [i n]
         (mock/a-user-exists))

       (fetch-all) =>
       (check [response]
         response => seq?
        (count response) => 20)

       (fetch-all {} {:page 2}) =>
       (check [response]
         response => seq?
         (count response) => (- n 20)))))

 (context #'get-domain
   (context "when passed nil"
     (get-domain nil) => (throws Exception))
   (context "when passed a user"
     (get-domain user-a) => domain-a))

 (context #'local?
   (context "when passed a user"
     (context "and it's domain is the same as the current domain"
       (let [user (mock/a-user-exists)]
         (local? user) => true))
     (context "and it's domain is different from the current domain"
       (let [user (mock/a-remote-user-exists)]
         (local? user) => false))))

 (context #'display-name
   (display-name .user.) => string?)

 (context #'get-link
   (let [user (factory :user {:links [{:rel "foo" :href "bar"}]})]
     (get-link user "foo" nil) => (contains {:href "bar"})
     (get-link user "baz" nil) => nil))

 (context #'get-user
   (context "when the user is found"
     (let [user (mock/a-user-exists)
           username (:username user)
           domain (actions.user/get-domain user)]
       (get-user username (:_id domain)) => user))

   (context "when the user is not found"
     (drop!)
     (let [username (fseq :id)
           domain (mock/a-domain-exists)]
       (get-user username (:_id domain)) => nil)))

 (context #'fetch-by-domain
   (let [domain (actions.domain/current-domain)
         user (mock/a-user-exists)]
     (fetch-by-domain domain) => (contains user)))

 (context #'user-meta-uri
   (context "when the user's domain does not have a lrdd link"
     (model.domain/drop!)
     (let [user (mock/a-user-exists)]
       (user-meta-uri user) => (throws RuntimeException)))

   (context "when the user's domain has a lrdd link"
     (let [domain (mock/a-remote-domain-exists)
           links [{:rel "lrdd"
                   :template "http://example.com/main/xrd?uri={uri}"}]]
       (model.domain/add-links domain links)

       (let [user (mock/a-remote-user-exists {:domain domain})]
         (user-meta-uri user) => (str "http://example.com/main/xrd?uri=" (get-uri user))))))

 )
