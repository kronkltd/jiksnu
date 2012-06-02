(ns jiksnu.xmpp.element-test
  (:use [ciste.config :only [with-environment]]
        ciste.core
        ciste.sections.default
        [clj-factory.core :only [factory]]
        jiksnu.test-helper
        jiksnu.model
        jiksnu.session
        jiksnu.xmpp.element
        midje.sweet)
  (:require [clj-tigase.element :as element]
            [jiksnu.namespace :as namespace]
            jiksnu.sections.activity-sections)
  (:import jiksnu.model.Activity))

(test-environment-fixture

 (fact "abdera-to-tigase-element"
   (fact "should return a tigase element"
     (with-context [:xmpp :atom]
       (let [element (show-section (factory Activity))]
         (abdera-to-tigase-element element) => element/element?)))))
