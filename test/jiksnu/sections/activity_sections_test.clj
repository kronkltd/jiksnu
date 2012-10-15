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
            [jiksnu.existance-helpers :as existance]
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
   (like-button (model/map->Activity (factory :activity))) =>
   (every-checker
    vector?
    #(= :form (first %))))

 ;; TODO: acl-link
 ;; TODO: show-comment
 ;; TODO: comment-link-item
 ;; TODO: index-formats
 ;; TODO: timeline-formats
 ;; TODO: pictures-section
 ;; TODO: tag-section
 ;; TODO: location-section
 ;; TODO: add-button-section
 ;; TODO: privacy select
 ;; TODO: post-actions
 ;; TODO: recipients-section
 ;; TODO: links section
 ;; TODO: tags section

 (fact "#'posted-link-section"
   (let [user (existance/a-user-exists)
         activity (existance/there-is-an-activity)]
     (posted-link-section activity) =>
     (every-checker
      #(h/html %))))
 
 (fact "#'uri Activity"
   (fact "should be a string"
     ;; TODO: not a good test
     (with-context [:http :html]
       (uri .activity.)) =>
       (every-checker
        string?)))

 (fact "index-block"
   (fact "when the context is [:http :rdf]"
     (with-context [:http :rdf]
       (let [activity (existance/there-is-an-activity)]
         (index-block [activity]) =>
         (every-checker
          (partial every? (fn [t]
                            (and (vector? t)
                                 (= 3 (count t))))))))))
 
 (fact "index-section"
   (fact "when the context is [:http :rdf]"
     (with-context [:http :rdf]
       (let [activity (existance/there-is-an-activity)]
         (index-section [activity]) =>
         (every-checker
          (fn [r]
            (for [t r]
              (fact
                t => vector?
                (count t) => 3))))))))
 
 (fact "#'show-section Activity :atom"
   (fact "should return an abdera entry"
     (with-context [:http :atom]
       (let [activity (existance/there-is-an-activity)]
         (show-section activity) =>
         (every-checker
          (fn [response]
            (fact
              response => (partial instance? Entry)
              (.getId response) => (partial instance? IRI)
              (.getUpdated response) => (partial instance? DateTime)
              (.getTitle response) => string?
              (.getAuthor response) => (partial instance? Person))))))))

 (fact "#'show-section Activity :xmpp"
   (fact "should return an element"
     (with-context [:xmpp :xmpp]
       (let [activity (existance/there-is-an-activity)]
         (show-section activity)) => element/element?)))
)
