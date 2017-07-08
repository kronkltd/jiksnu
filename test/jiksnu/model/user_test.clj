(ns jiksnu.model.user-test
  (:require [clj-factory.core :refer [factory fseq]]
            [jiksnu.modules.core.actions.domain-actions :as actions.domain]
            [jiksnu.modules.core.actions.user-actions :as actions.user]
            [jiksnu.mock :as mock]
            [jiksnu.model.domain :as model.domain]
            [jiksnu.model.user :refer :all]
            [jiksnu.test-helper :as th]
            [midje.sweet :refer :all])
  (:import jiksnu.model.User))

(th/module-test ["jiksnu.modules.core"])

(facts "#'count-records"
  (fact "when there aren't any items"
    (drop!)
    (count-records) => 0)
  (fact "when there are items"
    (drop!)
    (let [n 15]
      (dotimes [i n]
        (mock/a-user-exists))
      (count-records) => n)))

(facts "#'delete"
  (let [item (mock/a-user-exists)]
    (delete item) => item
    (fetch-by-id (:_id item)) => nil))

(facts "#'drop!"
  (dotimes [i 1]
    (mock/a-user-exists))
  (drop!)
  (count-records) => 0)

(facts "#'fetch-by-id"
  (fact "when the item doesn't exist"
    (let [id "acct:foo@bar.baz"]
      (fetch-by-id id) => nil?))

  (fact "when the item exists"
    (let [item (mock/a-user-exists)]
      (fetch-by-id (:_id item)) => item)))

(facts "#'create"
  (fact "when given valid params"
    (let [params (actions.user/prepare-create
                  (factory :local-user))]
      (create params) => (partial instance? User)))

  (fact "when given invalid params"
    (create {}) => (throws RuntimeException)))

(facts "#'fetch-all"
  (fact "when there are no items"
    (drop!)
    (fetch-all) => empty?)

  (fact "when there is more than a page of items"
    (drop!)

    (let [n 25]
      (dotimes [i n]
        (mock/a-user-exists))

      (fetch-all) => #(= (count %) 20)
      (fetch-all {} {:page 2}) => #(= (count %) (- n 20)))))

(facts "#'get-domain"
  (fact "when passed nil"
    (get-domain nil) => (throws Exception))
  (fact "when passed a user"
    (let [domain-a (actions.domain/current-domain)
          user-a (mock/a-user-exists)]
      (get-domain user-a) => domain-a)))

(facts "#'local?"
  (fact "when passed a user"
    (fact "and it's domain is the same as the current domain"
      (let [user (mock/a-user-exists)]
        (local? user) => true))
    (fact "and it's domain is different from the current domain"
      (let [user (mock/a-remote-user-exists)]
        (local? user) => false))))

(facts "#'display-name"
  (display-name .user.) => string?)

(facts "#'get-user"
  (fact "when the user is found"
    (let [user (mock/a-user-exists)
          username (:username user)
          domain (actions.user/get-domain user)]
      (get-user username (:_id domain)) => user))

  (fact "when the user is not found"
    (drop!)
    (let [username (fseq :id)
          domain (mock/a-domain-exists)]
      (get-user username (:_id domain)) => nil)))

(facts "#'fetch-by-domain"
  (let [domain (actions.domain/current-domain)
        user (mock/a-user-exists)]
    (fetch-by-domain domain) => (contains user)))

(facts "#'user-meta-uri"
  (fact "when the user's domain does not have a lrdd link"
    (model.domain/drop!)
    (let [user (mock/a-user-exists)]
      (user-meta-uri user) => (throws RuntimeException)))

  (fact "when the user's domain has a lrdd link"
    (let [domain (mock/a-remote-domain-exists)
          links [{:rel "lrdd"
                  :template "http://example.com/main/xrd?uri={uri}"}]]
      (model.domain/add-links domain links)

      (let [user (mock/a-remote-user-exists {:domain domain})]
        (user-meta-uri user) => (str "http://example.com/main/xrd?uri=" (get-uri user))))))
