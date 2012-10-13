(ns jiksnu.sections.user-sections-test
  (:use [ciste.config :only [with-environment]]
        [ciste.core :only [with-context with-format with-serialization]]
        [ciste.sections.default :only [uri show-section title]]
        [clj-factory.core :only [factory]]
        jiksnu.test-helper
        jiksnu.session
        jiksnu.sections.user-sections
        [midje.sweet :only [fact => every-checker]])
  (:require [clj-tigase.element :as element]
            [clojure.tools.logging :as log]
            [hiccup.core :as h]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.model :as model]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.domain :as model.domain]
            [jiksnu.model.user :as model.user]
            [jiksnu.xmpp.element :as xmpp.element])
  (:import java.io.StringWriter
           javax.xml.namespace.QName
           jiksnu.model.Activity
           jiksnu.model.Domain
           jiksnu.model.User
           org.apache.abdera2.common.iri.IRI
           org.apache.abdera2.model.Entry
           org.apache.abdera2.model.Person
           org.joda.time.DateTime
           tigase.xml.Element))

(test-environment-fixture
 (fact "uri User :html :http"
   (fact "should return a link to that user"
     (with-context [:http :html]       
       (let [user (model.user/create (factory :user))]
         (uri user) => string?))))

 (fact "title User"
   (fact "should return the title of that user"
     (with-context [:http :html]
       (let [user (model.user/create (factory :user))
             response (title user)]
         response => string?))))

 (fact "show-section User"
   (fact "when the serialization is :xmpp"
     (with-serialization :xmpp
       (fact "when the format is :xmpp"
         (with-format :xmpp
           (fact "should return a vcard string"
             (let [user (model.user/create (factory :user))]
               (show-section user) => string?))))))
   (fact "when the serialization is :http"
     (with-serialization :http
       (fact "when the format is :html"
         (with-format :html)))))
 )
