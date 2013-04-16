(ns jiksnu.sections.activity-sections-test
  (:use [ciste.config :only [with-environment]]
        [ciste.core :only [with-context]]
        [ciste.sections.default :only [index-block index-section
                                       show-section uri]]
        [clj-factory.core :only [factory]]
        jiksnu.test-helper
        jiksnu.session
        jiksnu.sections.activity-sections
        [midje.sweet :only [fact future-fact => every-checker]])
  (:require [clj-tigase.element :as element]
            [clojure.tools.logging :as log]
            [hiccup.core :as h]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.mock :as mock]
            [jiksnu.features-helper :as feature]
            [jiksnu.model :as model]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.user :as model.user])
  (:import jiksnu.model.User
           org.apache.abdera2.common.iri.IRI
           org.apache.abdera2.model.Entry
           org.apache.abdera2.model.Person
           org.joda.time.DateTime
           tigase.xml.Element))

(test-environment-fixture

 (future-fact "#'like-button"
   (like-button (factory :activity)) =>
   (every-checker
    vector?
    #(= :form (first %))))

 (fact "#'posted-link-section"
   (let [user (mock/a-user-exists)
         activity (mock/there-is-an-activity)]
     (posted-link-section activity) =>
     (every-checker
      #(h/html %))))

 (fact "#'uri Activity"
   ;; TODO: not a good test
   (with-context [:http :html]
     (uri .activity.)) =>
     (every-checker
      string?))

 (fact "index-block"
   (fact "when the context is [:http :rdf]"
     (with-context [:http :rdf]
       (let [activity (mock/there-is-an-activity)]
         (index-block [activity]) =>
         (every-checker
          (partial every? (fn [t]
                            (and (vector? t)
                                 (= 3 (count t))))))))))

 (fact "index-section"
   (fact "when the context is [:http :rdf]"
     (with-context [:http :rdf]
       (let [activity (mock/there-is-an-activity)]
         (index-section [activity]) =>
         (every-checker
          (fn [r]
            (for [t r]
              (fact
                t => vector?
                (count t) => 3))))))))

 (fact "#'show-section Activity :atom"
   (let [activity (mock/there-is-an-activity)]
     (with-context [:http :atom]
       (show-section activity)) =>
       (every-checker
        (fn [response]
          (fact
            response => (partial instance? Entry)
            (.getId response) => (partial instance? IRI)
            (.getUpdated response) => (partial instance? DateTime)
            (.getTitle response) => string?
            (.getAuthor response) => (partial instance? Person))))))

 (fact "#'show-section Activity :xmpp"
   (let [activity (mock/there-is-an-activity)]
     (with-context [:xmpp :xmpp]
       (show-section activity))) => element/element?)
 )
