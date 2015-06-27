(ns jiksnu.modules.as.views.subscription-views-test
  (:require [ciste.core :refer [with-serialization with-format]]
            [ciste.filters :refer [filter-action]]
            [ciste.views :refer [apply-view]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.subscription-actions :as actions.subscription]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.db :as db]
            [jiksnu.mock :as mock]
            [jiksnu.model :as model]
            [jiksnu.model.subscription :as model.subscription]
            [jiksnu.model.user :as model.user]
            [jiksnu.modules.as.views.subscription-views :as views.subsrciption]
            [jiksnu.modules.web.filters.subscription-filters :as filters.subsrciption]
            [jiksnu.test-helper :as th]
            [midje.sweet :refer :all]))

(namespace-state-changes
 [(before :contents (th/setup-testing))
  (after :contents (th/stop-testing))])

(fact "apply-view #'actions.subscription/get-subscriptions"
  (let [action #'actions.subscription/get-subscriptions]
    (fact "when the serialization is :http"
      (with-serialization :http
        (fact "when the format is :as"
          (with-format :as
            (fact "when the user has subscriptions"
              (db/drop-all!)
              (let [subscription (mock/a-subscription-exists)
                    actor (model.subscription/get-actor subscription)
                    request {:action action}
                    response (filter-action action request)]
                (let [response (apply-view request response)]
                  response => map?
                  (let [body (:body response)]
                    (:totalItems body) => (:totalItems response)))))))
        ))))


