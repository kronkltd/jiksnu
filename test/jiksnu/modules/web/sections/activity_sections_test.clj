(ns jiksnu.modules.web.sections.activity-sections-test
  (:require [ciste.core :refer [with-context]]
            [ciste.sections.default :refer [show-section uri]]
            jiksnu.modules.web.sections.activity-sections
            [jiksnu.test-helper :as th]
            [midje.sweet :refer :all])
  (:import jiksnu.model.Activity))

(namespace-state-changes
 [(before :contents (th/setup-testing))
  (after :contents (th/stop-testing))])

(fact #'uri
  (fact Activity
    ;; TODO: not a good test
    (with-context [:http :html]
      (uri .activity.)) => string?))
