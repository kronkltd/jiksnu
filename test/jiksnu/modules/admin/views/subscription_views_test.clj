(ns jiksnu.modules.admin.views.subscription-views-test
  (:require [ciste.core :refer [with-context with-serialization with-format]]
            [ciste.filters :refer [filter-action]]
            [ciste.views :refer [apply-view]]
            [clj-factory.core :refer [factory]]
            [clojure.tools.logging :as log]
            [clojurewerkz.support.http.statuses :as status]
            [hiccup.core :as h]
            [jiksnu.modules.admin.actions.subscription-actions :as actions.admin.subscription]
            [jiksnu.db :as db]
            [jiksnu.mock :as mock]
            [jiksnu.model :as model]
            [jiksnu.model.subscription :as model.subscription]
            [jiksnu.test-helper :refer [check test-environment-fixture]]
            [midje.sweet :refer [=> fact]]))

(test-environment-fixture

 (fact "apply-view #'actions.admin.subscription/delete [:http :html]"
   (let [action #'actions.admin.subscription/delete]
     (with-context [:http :html]

       (fact "when there is a subscription"
         (let [subscription (mock/a-subscription-exists)
               request {:action action
                        :params {:id (str (:_id subscription))}}
               response (filter-action action request)]
           (let [response (apply-view request response)]
             response => map?
             (:status response) => status/redirect?))))))

 )
