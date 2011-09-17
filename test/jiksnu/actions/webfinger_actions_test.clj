(ns jiksnu.actions.webfinger-actions-test
  (:use (ciste [debug :only (spy)])
        clj-factory.core
        clojure.test
        midje.sweet
        (jiksnu core-test)
        jiksnu.actions.webfinger-actions)
  (:require (jiksnu.actions [user-actions :as actions.user])
            (jiksnu.model [user :as model.user]))
  (:import com.cliqset.xrd.XRD
           jiksnu.model.User
           org.openxrd.xrd.core.impl.XRDImpl
           com.cliqset.hostmeta.HostMetaException))

(use-fixtures :each test-environment-fixture)

(deftest test-fetch
  (testing "when the url points to a valid XRD document"
    (fact
      (let [url "http://kronkltd.net/.well-known/host-meta"]
        (fetch url) => (partial instance? XRD))))
  (testing "when the url does not point to a valid XRD document"
    (fact "should raise an exception"
      (let [url "http://example.com/.well-known/host-meta"]
        (fetch url) => (throws RuntimeException #_HostMetaException)))))

(deftest test-host-meta
  (fact
    (host-meta) => (partial instance? XRDImpl)))

(deftest test-user-meta
  (testing "when the url matches a known user"
    (fact
      (let [user (actions.user/create (factory User))
            uri (model.user/get-uri user)]
        (user-meta uri) => (partial instance? User)
        (user-meta uri) => user))))

(deftest test-parse-link)

(deftest test-get-user-meta-uri
  (testing "when the user meta link has been associated"
    (fact "should return that uri"
      (let [user-meta-uri (fseq :uri)
            user (actions.user/create
                  (factory User {:user-meta-uri user-meta-uri}))]
        (get-user-meta-uri user) => user-meta-uri))))

(deftest test-fetch-user-meta
  (fact "should return an xml stream"
    (let [user (actions.user/create (factory User))]
      (fetch-user-meta user)) => nil))

(deftest test-get-links
  (fact
    (let [xrd (XRD.)]
      (get-links xrd)) => seq?))

(deftest test-get-keys-from-xrd
  (future-fact "should return a sequence of keys for the uri"
    (let [uri "acct:duck@kronkltd.net"]
      (get-keys uri)) => seq?))
