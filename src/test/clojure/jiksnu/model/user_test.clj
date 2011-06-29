(ns jiksnu.model.user-test
  (:use ciste.config
        clj-factory.core
        clojure.test
        jiksnu.core-test
        jiksnu.model
        jiksnu.model.user)
  (:import jiksnu.model.User))

(use-fixtures :each test-environment-fixture)

(deftest get-id-test)

(deftest get-domain-test)

(deftest bare-jid-test)

(deftest split-uri-test)

(deftest rel-filter-test)

(deftest rel-filter-feed-test)

(deftest get-link-test)

(deftest drop!-test)

(deftest create-test)

(deftest index-test
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

(deftest show-test
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

(deftest fetch-by-id-test)

(deftest fetch-by-jid-test)

(deftest fetch-by-uri-test)

(deftest find-or-create-test)

(deftest find-or-create-by-uri-test)

(deftest find-or-create-by-jid-test)

(deftest subnodes-test)

(deftest edit-test
  (testing "when the user is found"
    (testing "should return a user" :pending))
  (testing "when the user is not found"
    (testing "should return nil" :pending)))

(deftest delete-test
  (testing "when the user exists"
    (testing "should be deleted" :pending)))

(deftest add-node-test)

(deftest inbox-test)

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

(deftest get-uri-test)

(deftest author-uri-test)

(deftest get-domain-test)

(deftest user-meta-uri-test)

(deftest rule-element?-test)
