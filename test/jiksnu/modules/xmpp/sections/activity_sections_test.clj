(ns jiksnu.modules.xmpp.sections.activity-sections-test
  (:use [ciste.core :only [with-context with-format with-serialization]]
        [ciste.sections.default :only [show-section]]
        [jiksnu.test-helper :only [check context future-context test-environment-fixture]]
        [midje.sweet :only [=>]])
  (:require [clj-tigase.element :as element]
            [clojure.tools.logging :as log]
            [jiksnu.mock :as mock])
  (:import jiksnu.model.Activity))

(test-environment-fixture
 (context #'show-section
   (context Activity
     (context ":xmpp"
       (let [activity (mock/there-is-an-activity)]
         (with-context [:xmpp :xmpp]
           (show-section activity))) => element/element?)))

 )
