(ns jiksnu.sections.activity-sections-test
  (:use [ciste.config :only [with-environment]]
        [ciste.core :only [with-context with-format with-serialization]]
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
            [jiksnu.ko :as ko]
            [jiksnu.mock :as mock]
            [jiksnu.features-helper :as feature]
            [jiksnu.model :as model]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.user :as model.user])
  (:import jiksnu.model.Activity
           jiksnu.model.User
           org.apache.abdera.i18n.iri.IRI
           org.apache.abdera.model.Entry
           org.apache.abdera.model.Person
           org.joda.time.DateTime
           tigase.xml.Element))

(test-environment-fixture

 (future-fact "#'like-button"
   (like-button (factory :activity)) =>
   (every-checker
    vector?
    #(= :form (first %))))

 (fact "#'posted-link-section"
   (fact "when the serialization is :http"
     (with-serialization :http

       (fact "when the format is :html"
         (with-format :html

           (fact "when dynamic is false"
             (binding [ko/*dynamic* false]

               (fact "when given an empty activity"
                 (let [item (Activity.)]
                   (posted-link-section item) =>
                   (fn [response]
                     (fact
                       (let [resp-str (h/html response)]
                         resp-str => string?)))))

               (fact "when given a real activity"
                 (let [activity (mock/there-is-an-activity)]
                   (posted-link-section activity) =>
                   (fn [response]
                     (fact
                       (let [resp-str (h/html response)]
                         resp-str => string?)))))
               ))
           ))
       ))
   )

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

 (fact "#'index-section Activity"
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

 (fact "#'show-section Activity"
   (fact "when the serialization is :http"
     (with-serialization :http

       (fact "when the format is :atom"
         (with-format :atom

          (let [activity (mock/there-is-an-activity)]
            (show-section activity) =>
            (fn [response]
              (fact
                response => (partial instance? Entry)
                (.getId response) => (partial instance? IRI)
                (.getUpdated response) => (partial instance? DateTime)
                (.getTitle response) => string?
                (.getAuthor response) => (partial instance? Person))))))

       (fact "when the format is :html"
         (with-format :html

           (fact "when dynamic is false"
             (binding [ko/*dynamic* false]

               (fact "when given an empty activity"
                 (let [item (Activity.)]
                   (show-section item) =>
                   (fn [response]
                     (fact
                       (let [resp-str (h/html response)]
                         resp-str => string?)))))

               (fact "when given a real activity"
                 (let [activity (mock/there-is-an-activity)]
                   (show-section activity) =>
                   (fn [response]
                     (fact
                       (let [resp-str (h/html response)]
                         resp-str => string?)))))
               ))
           ))
       ))

   (fact ":xmpp"
     (let [activity (mock/there-is-an-activity)]
       (with-context [:xmpp :xmpp]
         (show-section activity))) => element/element?)
   )
 )
