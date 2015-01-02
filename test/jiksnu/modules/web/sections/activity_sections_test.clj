(ns jiksnu.modules.web.sections.activity-sections-test
  (:use [ciste.config :only [with-environment]]
        [ciste.core :only [with-context with-format with-serialization]]
        [ciste.sections.default :only [show-section uri]]
        [clj-factory.core :only [factory]]
        jiksnu.modules.web.sections.activity-sections
        [jiksnu.test-helper :only [check test-environment-fixture]]
        [midje.sweet :only [=> fact future-fact]])
  (:require [clojure.tools.logging :as log]
            [hiccup.core :as h]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.ko :as ko]
            [jiksnu.mock :as mock]
            [jiksnu.model :as model]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.user :as model.user])
  (:import jiksnu.model.Activity
           jiksnu.model.User
           org.apache.abdera.i18n.iri.IRI
           org.apache.abdera.model.Entry
           org.apache.abdera.model.Person
           org.joda.time.DateTime))

(test-environment-fixture

 (future-fact #'like-button
   (like-button (factory :activity)) =>
   (check [response]
     response => vector?

     ;; TODO: This checks that the first element is a form. This is no
     ;; longer a good test.
     (first response) => :form))

 (fact #'posted-link-section
   (fact "when the serialization is :http"
     (with-serialization :http

       (fact "when the format is :html"
         (with-format :html

           (fact "when dynamic is false"
             (binding [ko/*dynamic* false]

               (fact "when given an empty activity"
                 (let [item (Activity.)]
                   (posted-link-section item) =>
                   (check [response]
                     (let [resp-str (h/html response)]
                       resp-str => string?))))

               (fact "when given a real activity"
                 (let [activity (mock/there-is-an-activity)]
                   (posted-link-section activity) =>
                   (check [response]
                     (let [resp-str (h/html response)]
                       resp-str => string?))))
               ))
           ))
       ))
   )

 (fact #'uri
   (fact Activity
     ;; TODO: not a good test
     (with-context [:http :html]
       (uri .activity.)) => string?))

 (fact #'show-section
   (fact Activity
     (fact "when the serialization is :http"
       (with-serialization :http

         (fact "when the format is :html"
           (with-format :html

             (fact "when dynamic is false"
               (binding [ko/*dynamic* false]

                 (fact "when given an empty activity"
                   (let [item (Activity.)]
                     (show-section item) =>
                     (check [response]
                       (let [resp-str (h/html response)]
                         resp-str => string?))))

                 (fact "when given a real activity"
                   (let [activity (mock/there-is-an-activity)]
                     (show-section activity) =>
                     (check [response]
                       (let [resp-str (h/html response)]
                         resp-str => string?))))
                 ))
             ))
         )))

   )
 )
