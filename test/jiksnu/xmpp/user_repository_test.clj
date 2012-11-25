(ns jiksnu.xmpp.user-repository-test
  (:use [clj-factory.core :only [factory fseq]]
        [jiksnu.test-helper :only [test-environment-fixture]]
        [midje.sweet :only [fact future-fact =>]])
  (:import jiksnu.xmpp.user_repository))

(def this (user_repository.))

(test-environment-fixture

 ;; TODO: actually create some users and test different counts
 (future-fact ".getUsersCount"
   (fact "when not given a domain"
     (.getUsersCount this) => 0)
   (fact "when given a domain"
     (let [domain (fseq :domain)]
       (.getUsersCount this domain) => 0)))

 )
