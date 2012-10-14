(ns jiksnu.sections.activity-sections-test
  (:use [ciste.config :only [with-environment]]
        [ciste.core :only [with-context]]
        [ciste.sections.default :only [index-block index-section show-section uri]]
        [clj-factory.core :only [factory]]
        jiksnu.test-helper jiksnu.session
        jiksnu.sections.activity-sections
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

 (fact "#'like-button"
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
   (let [activity (actions.activity/create (factory :activity))]
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
       (let [user (model.user/create (factory :local-user))
             author-map {:author (:_id user)}
             activity (model.activity/create (factory :activity author-map))]
         (index-block [activity]) =>
         (every-checker
          (partial every? (fn [t]
                            (and (vector? t)
                                 (= 3 (count t))))))))))
 
 (fact "index-section"
   (fact "when the context is [:http :rdf]"
     (with-context [:http :rdf]
       (let [user (model.user/create (factory :local-user))
             author-map {:author (:_id user)}
             activity (model.activity/create (factory :activity author-map))]
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
       (let [user (model.user/create (factory :local-user))
             author-map {:author (:_id user)}
             activity (model.activity/create (factory :activity author-map))]
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
       (let [actor (model.user/create (factory :user))]
         (with-user actor
           (let [entry (model.activity/create (factory :activity))]
             (show-section entry)))) => element/element?)))
)
