(ns jiksnu.actions.webfinger-actions-test
  (:use [ciste.config :only [config]]
        [clj-factory.core :only [fseq]]
        [jiksnu.actions.webfinger-actions :only [host-meta]]
        [jiksnu.test-helper :only [test-environment-fixture]]
        [midje.sweet :only [fact every-checker]])
  (:require [clojure.tools.logging :as log]
            jiksnu.factory))

(test-environment-fixture

 (fact "#'host-meta"
   (let [domain (config :domain)]
     (host-meta) => (every-checker
                     map?
                     #(= domain (:host %))
                     #(>= 1 (count (:links %))))))

 )
