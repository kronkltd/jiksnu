(ns jiksnu.modules.web.views.domain-views-test
  (:require [ciste.core :refer [with-context]]
            [ciste.filters :refer [filter-action]]
            [ciste.views :refer [apply-view]]
            [clj-factory.core :refer [factory]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.test-helper :refer [test-environment-fixture]]
            [midje.sweet :refer [=> facts fact]])
  (:import jiksnu.model.Domain))


(test-environment-fixture

 ;; TODO: going away
 (facts "apply-view #'actions.domain/show [:http :html]"
   (let [action #'actions.domain/show]
     (with-context [:http :html]

      (let [domain (Domain.)
            request {:action action
                     :params {:id (:_id domain)}}
            response (filter-action action request)
            rendered (apply-view request response)]

        (fact "returns a map"
          (log/spy :info rendered) => map?)
        )

      )
     ))
 )
