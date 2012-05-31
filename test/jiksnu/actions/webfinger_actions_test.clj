(ns jiksnu.actions.webfinger-actions-test
  (:use [ciste.config :only [config]]
        [ciste.debug :only [spy]]
        [clj-factory.core :only [fseq]]
        [jiksnu.actions.webfinger-actions :only [host-meta]]
        [jiksnu.test-helper :only [test-environment-fixture]]
        [midje.sweet :only [fact every-checker]])
  (:require jiksnu.factory))

(test-environment-fixture
 (fact "#'host-meta"
   (let [domain (config :domain)]
     (host-meta) => (every-checker
                     map?
                     ;; #(spy %)
                     #(= domain (:host %))
                     #(>= 1 (count (:links %)))
                     ))))
