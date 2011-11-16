(ns jiksnu.model.webfinger-test
  (:use (ciste [debug :only (spy)])
        (clj-factory [core :only (factory)])
        clojure.test
        midje.sweet
        (jiksnu core-test model)
        jiksnu.model.webfinger)
  (:require (jiksnu.actions [user-actions :as actions.user]))
  (:import jiksnu.model.User
           nu.xom.Document))



(use-fixtures :once test-environment-fixture)

(background
 (around :facts
   (let [user (actions.user/create (factory User))
         options {}]
     ?form)))

(deftest test-fetch-host-meta
  (testing "when the url points to a valid XRD document"
    (fact
      (let [url "http://kronkltd.net/.well-known/host-meta"]
        (fetch-host-meta url) => (partial instance? Document))))
  (testing "when the url does not point to a valid XRD document"
    (future-fact "should raise an exception"
      (let [url "http://example.com/.well-known/host-meta"]
        (fetch-host-meta url) => nil))))

;; (deftest test-get-links
;;   (future-fact
;;     (let [xrd nil]
;;       (get-links xrd)) => seq?))

;; (deftest test-get-keys-from-xrd
;;   (future-fact "should return a sequence of keys for the uri"
;;     (let [uri "acct:duck@kronkltd.net"]
;;       (get-keys uri)) => seq?))

