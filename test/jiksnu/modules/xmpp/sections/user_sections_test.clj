(ns jiksnu.modules.xmpp.sections.user-sections-test
  (:use [ciste.core :only [with-context with-format with-serialization]]
        [ciste.sections.default :only [show-section]]
        [clj-factory.core :only [factory]]
        [jiksnu.test-helper :only [check context future-context test-environment-fixture]]
        jiksnu.session
        [midje.sweet :only [=>]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.mock :as mock]
            [jiksnu.model :as model]
            [jiksnu.model.user :as model.user]
            jiksnu.modules.xmpp.sections.user-sections)
  (:import jiksnu.model.User))

(test-environment-fixture

 (context "show-section User"
   (context "when the serialization is :xmpp"
     (with-serialization :xmpp
       (context "when the format is :xmpp"
         (with-format :xmpp
           (context "should return a vcard string"
             (let [user (mock/a-user-exists)]
               (show-section user) =>
               (check [response]
                 response => string?
                 response => #"<vcard"))))))))

 )
