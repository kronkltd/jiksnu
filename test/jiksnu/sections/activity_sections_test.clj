(ns jiksnu.sections.activity-sections-test
  (:use [ciste.config :only [with-environment]]
        [ciste.core :only [with-context]]
        ciste.sections.default
        [clj-factory.core :only [factory]]
        jiksnu.test-helper jiksnu.session
        jiksnu.sections.activity-sections
        midje.sweet)
  (:require [clj-tigase.element :as element]
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
           org.apache.abdera2.model.Entry
           org.apache.abdera2.model.Person
           tigase.xml.Element))

(test-environment-fixture

 (fact "#'uri Activity"
   (facts "should be a string"
     (uri activity) => string?
     (against-background
       (around
        :facts
        (with-context [::http :html]                                        
          (with-user (model.user/create (factory User))
            (let [activity (model.activity/create (factory Activity))]
              ?form)))))))

 (fact "#'show-section Activity :atom"
   (facts "should return an abdera entry"
     (with-context [:http :atom]
       (let [domain (model.domain/create (factory Domain))
             user (model.user/create (factory User {:domain (:_id domain)}))
             author-map {:author (:_id user)}
             activity (model.activity/create (factory Activity author-map))]
         (let [response (show-section activity)]
           (instance? Entry response) => truthy
           (.getId response) => truthy
           (.getTitle response) => truthy
           (.getUpdated response) => truthy
           (.getAuthor response) => (partial instance? Person)
           )))))

 (fact "#'show-section Activity :xmpp"
   (facts "should return an element"
     (show-section entry) => element/element?
     (against-background
       (around
        :facts
        (with-context [:xmpp :xmpp]
          (with-user (model.user/create (factory User))
            (let [entry (model.activity/create (factory Activity))]
              ?form))))))))
