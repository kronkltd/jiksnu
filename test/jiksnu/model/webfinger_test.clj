(ns jiksnu.model.webfinger-test
  (:use (ciste [config :only [with-environment]]
               [debug :only (spy)])
        (clj-factory [core :only (factory)])
        midje.sweet
        (jiksnu test-helper model)
        jiksnu.model.webfinger)
  (:require (jiksnu.actions [user-actions :as actions.user]))
  (:import jiksnu.model.User
           nu.xom.Document))

(test-environment-fixture

 ;; TODO: Mock these, don't actually request
 (fact "#'fetch-host-meta"
   (fact "when the url points to a valid XRD document"
     ;; TODO: pick a random domain
     (let [url "http://kronkltd.net/.well-known/host-meta"]
       (fetch-host-meta url) => (partial instance? Document)))
   
   (fact "when the url does not point to a valid XRD document"
     (fact "should raise an exception"
       (let [url "http://example.com/.well-known/host-meta"]
         (fetch-host-meta url) => nil))))
 
 (fact "#'get-links"
   (fact "When it has links"
     (fact "should return the sequence of links"
       (let [xrd nil]
         (get-links xrd)))) => seq?)

 ;; (fact "#'get-keys-from-xrd"
 ;;   (fact "should return a sequence of keys for the uri"
 ;;     (let [uri "acct:duck@kronkltd.net"]
 ;;       (get-keys uri)) => seq?))

 )
