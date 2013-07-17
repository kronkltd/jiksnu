(ns jiksnu.sections.user-sections-test
  (:use [ciste.core :only [with-context with-format with-serialization]]
        [ciste.sections.default :only [uri show-section title]]
        [clj-factory.core :only [factory]]
        [jiksnu.ko :only [*dynamic*]]
        [jiksnu.test-helper :only [check context future-context test-environment-fixture]]
        jiksnu.session
        [midje.sweet :only [=>]])
  (:require [clojure.tools.logging :as log]
            [hiccup.core :as h]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.mock :as mock]
            [jiksnu.features-helper :as feature]
            [jiksnu.model :as model]
            [jiksnu.model.user :as model.user])
  (:import jiksnu.model.User))

(test-environment-fixture

 (context "uri User :html :http"
   (context "when the serialization is :http"
     (with-serialization :http
       (context "when the format is :html"
         (with-format :html
           (context "when it is html-only"
             (binding [*dynamic* false]
               (let [user (mock/a-user-exists)]
                 (uri user) => string?))))))))

 (context "title User"
   (context "should return the title of that user"
     (with-context [:http :html]
       (let [user (mock/a-user-exists)
             response (title user)]
         response => string?))))

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
                 response => #"<vcard")))))))
   (context "when the serialization is :http"
     (with-serialization :http
       (context "when the format is :html"
         (with-format :html
           (binding [*dynamic* false]
             (let [user (mock/a-user-exists)]
               (show-section user))) =>
               (check [response]
                 (let [body (h/html response)]
                   body => #"user")))))))
 )
