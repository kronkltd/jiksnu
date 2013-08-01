(ns jiksnu.modules.xmpp.sections.activity-sections-test
  (:use [ciste.config :only [with-environment]]
        [ciste.core :only [with-context with-format with-serialization]]
        [ciste.sections.default :only [show-section]]
        [jiksnu.test-helper :only [check context future-context test-environment-fixture]]
        [midje.sweet :only [=>]])
  (:require [clj-tigase.element :as element]
            [clojure.tools.logging :as log]
            [jiksnu.mock :as mock])
  (:import jiksnu.model.Activity
           jiksnu.model.User
           org.apache.abdera.i18n.iri.IRI
           org.apache.abdera.model.Person
           org.joda.time.DateTime
           tigase.xml.Element))

(test-environment-fixture
 (context #'show-section
   (context Activity
     (context ":xmpp"
       (let [activity (mock/there-is-an-activity)]
         (with-context [:xmpp :xmpp]
           (show-section activity))) => element/element?)))

 )
