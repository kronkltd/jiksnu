(ns jiksnu.modules.admin.views.like-views-test
  (:require [ciste.core :refer [with-context]]
            [ciste.filters :refer [filter-action]]
            [ciste.views :refer [apply-view]]
            [clojure.tools.logging :as log]
            [clojurewerkz.support.http.statuses :as status]
            [jiksnu.actions.like-actions :as actions.like]
            [jiksnu.modules.admin.actions.like-actions :as actions.admin.like]
            jiksnu.modules.admin.filters.like-filters
            jiksnu.modules.admin.views.like-views
            [jiksnu.mock :as mock]
            [jiksnu.model :as model]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.feed-subscription :as model.feed-subscription]
            [jiksnu.test-helper :as th]
            [midje.sweet :refer :all]))

(namespace-state-changes
 [(before :contents (th/setup-testing))
  (after :contents (th/stop-testing))])


(fact "apply-view #'actions.admin.like/delete [:http :html]"
  (let [action #'actions.admin.like/delete]
    (with-context [:http :html]
      (let [user (mock/a-user-exists)
            activity (mock/there-is-an-activity)
            like (actions.like/like-activity activity user)
            request {:action action
                     :params {:id (str (:_id like))}}
            response (filter-action action request)]

         (apply-view request response) =>
         (contains {:status status/redirect?
                    :headers (contains {"Location" "/admin/likes"})})))))

