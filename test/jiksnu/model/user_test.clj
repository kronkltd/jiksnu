(ns jiksnu.model.user-test
  (:use ciste.config
        ciste.debug
        clj-factory.core
        clojure.test
        (jiksnu core-test model)
        jiksnu.model.user
        midje.sweet)
  (:require (jiksnu.model [domain :as model.domain]))
  (:import jiksnu.model.Domain
           jiksnu.model.User))

(use-fixtures :each test-environment-fixture)

(deftest test-split-uri
  (facts
    (split-uri "bob@example.com") => ["bob" "example.com"]
    (split-uri "acct:bob@example.com") => ["bob" "example.com"]
    (split-uri "http://example.com/bob") => nil))

(deftest test-rel-filter
  (facts
    (let [links [{:rel "alternate"}
                 {:rel "contains"}]]
      (rel-filter "alternate" links) => [{:rel "alternate"}]
      (rel-filter "foo" links) => [])))

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
    (testing "should be empty"
      (drop!)
      (let [response (index)]
        (is (empty? response)))))
  (testing "when there are users"
    (testing "should not be empty"
      (create (factory User))
      (let [response (index)]
        (is (seq response))))
    (testing "should return a seq of users"
      (create (factory User))
      (let [response (index)]
        (is (every? (partial instance? User) response))))))

(deftest test-show
  (testing "when the user is found"
    (testing "should return a user"
      (let [username (fseq :id)]
        (create (factory User {:username username}))
        (let [response (show username)]
          (is (instance? User response))))))
  (testing "when the user is not found"
    (testing "should return nil"
      (drop!)
      (let [username (fseq :id)]
        (let [response (show username)]
          (is (is (nil? response))))))))

(deftest test-fetch-by-id)

(deftest test-user-meta-uri
  (testing "when the user's domain does not have a lrdd link"
    (fact "should return nil"
     (let [user (factory User)]
       (user-meta-uri user) => nil)))
  (testing "when the user's domain has a lrdd link"
    (fact "should insert the user's uri into the template"
      (let [domain (model.domain/create
                    (factory Domain
                             {:links [{:rel "lrdd"
                                       :template "{uri}"}]}))
            user (create
                  (factory User {:domain (:_id domain)}))]
        (user-meta-uri user) => (get-uri user)))))







(deftest get-id-test)

(deftest get-domain-test)

(deftest bare-jid-test)

(deftest rel-filter-feed-test)

(deftest edit-test
  (testing "when the user is found"
    (testing "should return a user" :pending))
  (testing "when the user is not found"
    (testing "should return nil" :pending)))

(deftest delete-test
  (testing "when the user exists"
    (testing "should be deleted" :pending)))

(deftest update-test
  (testing "when the request is valid"
    (testing "should return a user"
      (let [request {:params {"id" (fseq :word)}}]))))

(deftest local?-test
  (testing "when there is a user"
    (testing "and it's domain is the same as the current domain"
      (testing "should be true"
        (let [user (factory User {:domain (-> (config) :domain)})]
          (is (local? user)))))
    (testing "and it's domain is different from the current domain"
      (testing "should be false"
        (let [user (factory User {:domain (fseq :domain)})]
          (is (not (local? user))))))))
