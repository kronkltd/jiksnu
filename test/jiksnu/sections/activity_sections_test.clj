(ns jiksnu.sections.activity-sections-test
  (:use [ciste.config :only [with-environment]]
        [ciste.core :only [with-context]]
        [ciste.sections.default :only [uri show-section]]
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

 (fact "#'show-section Activity :atom"
   (fact "should return an abdera entry"
     (with-context [:http :atom]
       (let [domain (actions.domain/find-or-create (factory Domain))
             user (model.user/create (factory User {:domain (:_id domain)}))
             author-map {:author (:_id user)}
             activity (model.activity/create (factory Activity author-map))]
         (show-section activity) =>
         (every-checker
          (partial instance? Entry)
          #(instance? IRI (.getId %))
          #(string? (.getTitle %))
          #(instance? DateTime (.getUpdated %))
          #(instance? Person (.getAuthor %)))))))

 (fact "#'show-section Activity :xmpp"
   (fact "should return an element"
     (with-context [:xmpp :xmpp]
       (let [actor (model.user/create (factory User))]
         (with-user actor
           (let [entry (model.activity/create (factory Activity))]
             (show-section entry)))) => element/element?)))
)
