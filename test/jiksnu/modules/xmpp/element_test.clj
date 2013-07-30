(ns jiksnu.modules.xmpp.element-test
  (:use [ciste.config :only [with-environment]]
        [ciste.core :only [with-context]]
        [ciste.sections.default :only [show-section]]
        [clj-factory.core :only [factory]]
        [jiksnu.test-helper :only [context future-context test-environment-fixture]]
        [jiksnu.modules.xmpp.element :only [abdera-to-tigase-element]]
        [midje.sweet :only [=>]])
  (:require [clj-tigase.element :as element]
            [jiksnu.model :as model]
            [jiksnu.namespace :as namespace]
            jiksnu.sections.activity-sections)
  (:import jiksnu.model.Activity))

(test-environment-fixture

 ;; just create a simple element here, don't involve activities
 (future-context "abdera-to-tigase-element"
   (context "should return a tigase element"
     (with-context [:xmpp :atom]
       (let [element (show-section (factory :activity))]
         (abdera-to-tigase-element element) => element/element?)))))
