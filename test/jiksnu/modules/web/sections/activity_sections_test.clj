(ns jiksnu.modules.web.sections.activity-sections-test
  (:require [ciste.core :refer [with-context]]
            [ciste.sections.default :refer [show-section uri]]
            [clj-factory.core :refer [factory]]
            [clojure.tools.logging :as log]
            [jiksnu.mock :as mock]
            [jiksnu.model :as model]
            jiksnu.modules.web.sections.activity-sections
            [jiksnu.test-helper :refer [test-environment-fixture]]
            [midje.sweet :refer [=> fact future-fact]])
  (:import jiksnu.model.Activity
           jiksnu.model.User
           org.joda.time.DateTime))

(test-environment-fixture

 (fact #'uri
   (fact Activity
     ;; TODO: not a good test
     (with-context [:http :html]
       (uri .activity.)) => string?))

 )
