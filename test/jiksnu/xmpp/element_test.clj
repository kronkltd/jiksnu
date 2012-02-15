(ns jiksnu.xmpp.element-test
  (:use (ciste [config :only [with-environment]]
               core
               [debug :only [spy]])
        ciste.sections.default
        [clj-factory.core :only [factory]]
        (jiksnu test-helper model session)
        jiksnu.xmpp.element
        [karras.entity :only [make]]
        midje.sweet)
  (:require [clj-tigase.element :as element]
            (jiksnu [namespace :as namespace])
            jiksnu.sections.activity-sections)
  (:import jiksnu.model.Activity))

(test-environment-fixture

  (fact "abdera-to-tigase-element"
    (fact "should return a tigase element"
      (with-context [:xmpp :atom]
        (let [element (show-section (factory Activity))]
          (abdera-to-tigase-element element) => element/element?)))))
