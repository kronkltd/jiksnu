(ns jiksnu.modules.web.sections.activity-sections-test
  (:require [ciste.core :refer [with-context]]
            [ciste.sections.default :refer [show-section uri]]
            [clj-factory.core :refer [factory]]
            [clojure.tools.logging :as log]
            [jiksnu.mock :as mock]
            [jiksnu.model :as model]
            jiksnu.modules.web.sections.activity-sections
            [jiksnu.test-helper :refer [check test-environment-fixture]]
            [midje.sweet :refer [=> fact future-fact]])
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

 (fact #'uri
   (fact Activity
     ;; TODO: not a good test
     (with-context [:http :html]
       (uri .activity.)) => string?))

 )
