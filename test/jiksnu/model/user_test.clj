(ns jiksnu.model.user-test
  (:use (ciste config debug)
        clj-factory.core
        clojure.test
        (jiksnu core-test model)
        jiksnu.model.user
        midje.sweet)
  (:require (jiksnu.actions [domain-actions :as actions.domain]
                            [user-actions :as actions.user])
            (jiksnu.model [domain :as model.domain]))
  (:import jiksnu.model.Domain
           jiksnu.model.User))

(use-fixtures :each test-environment-fixture)

(deftest test-get-domain
  (facts
    (let [domain (actions.domain/create (factory Domain))
          user (actions.user/create (factory User {:domain (:_id domain)}))]
      (get-domain nil) => nil
      (get-domain user) => domain)))

(deftest test-get-uri)

(deftest test-local?
  (testing "when there is a user"
    (testing "and it's domain is the same as the current domain"
      (fact "should be true"
        (let [domain (-> (config) :domain)
              user (factory User {:domain domain})]
          (local? user) => truthy)))
    (testing "and it's domain is different from the current domain"
      (fact "should be false"
        (let [domain (fseq :domain)
              user (factory User {:domain domain})]
          (local? user) => falsey)))))

(deftest test-rel-filter
  (facts
    (let [links [{:rel "alternate"}
                 {:rel "contains"}]]
      (rel-filter "alternate" links) => [{:rel "alternate"}]
      (rel-filter "foo" links) => [])))

(deftest test-split-uri
  (facts
    (split-uri "bob@example.com") => ["bob" "example.com"]
    (split-uri "acct:bob@example.com") => ["bob" "example.com"]
    (split-uri "http://example.com/bob") => nil))

(deftest test-display-name
  (facts
    (display-name .user.) => string?))

(deftest test-get-link
  (fact
    (let [user (factory User {:links [{:rel "foo" :href "bar"}]})]
      (get-link user "foo") => (contains {:href "bar"})
      (get-link user "baz") => nil)))

(deftest test-drop!)

(deftest test-create)

;; TODO: This is a better test for actions
(deftest test-index
  (testing "when there are no users"
    (fact "should be empty"
      ;; TODO: all collections should be emptied in background
      (drop!)
      (index) => empty?))
  (testing "when there are users"
    (fact "should not be empty"
      (actions.user/create (factory User))
      (index) => seq?)
    (fact "should return a seq of users"
      (actions.user/create (factory User))
      (index) => (partial every? user?))))

(deftest test-show
  (testing "when the user is found"
    (fact "should return a user"
      (let [username (fseq :id)]
        (actions.user/create (factory User {:username username}))
        (show username) => user?)))
  (testing "when the user is not found"
    (fact "should return nil"
      (drop!)
      (let [username (fseq :id)]
        (show username) => nil))))

(deftest test-fetch-by-id)

(deftest test-fetch-by-jid)

(deftest test-fetch-by-uri)

(deftest test-fetch-by-remote-id)

(deftest test-find-or-create)

(deftest test-find-or-create-by-uri)

(deftest test-find-or-create-by-remote-id)

(deftest test-find-or-create-by-jid)

(deftest test-subnodes)

(deftest edit-test
  (testing "when the user is found"
    (future-fact "should return a user"))
  (testing "when the user is not found"
    (future-fact "should return nil")))

(deftest delete-test
  (testing "when the user exists"
    (future-fact "should be deleted")))

(deftest test-add-node)

(deftest update-test
  (testing "when the request is valid"
    (future-fact "should return a user"
      (let [request {:params {"id" (fseq :word)}}]))))

(deftest test-user-meta-uri
  (testing "when the user's domain does not have a lrdd link"
    (fact "should return nil"
     (let [user (actions.user/create (factory User))]
       (user-meta-uri user) => nil)))
  (testing "when the user's domain has a lrdd link"
    (fact "should insert the user's uri into the template"
      (let [domain (actions.domain/create
                    (factory Domain
                             {:links [{:rel "lrdd"
                                       :template "{uri}"}]}))
            user (actions.user/create
                  (factory User {:domain (:_id domain)}))]
        (user-meta-uri user) => (get-uri user)))))

(deftest test-format-data)
