(ns jiksnu.modules.admin.views.like-views-test
  (:require [ciste.core :refer [with-context with-serialization with-format]]
            [ciste.filters :refer [filter-action]]
            [ciste.views :refer [apply-view]]
            [clj-factory.core :refer [factory]]
            [clojure.tools.logging :as log]
            [clojurewerkz.support.http.statuses :as status]
            [jiksnu.modules.admin.actions.like-actions :as actions.admin.like]
            [jiksnu.actions.like-actions :as actions.like]
            [jiksnu.mock :as mock]
            [jiksnu.model :as model]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.feed-subscription :as model.feed-subscription]
            [jiksnu.test-helper :refer [test-environment-fixture]]
            [midje.sweet :refer [=> fact]]))

(test-environment-fixture

 ;; (fact "apply-view #'actions.admin.like/delete [:http :html]"
 ;;   (let [action #'actions.admin.like/delete]
 ;;     (with-context [:http :html]
 ;;       (let [user (mock/a-user-exists)
 ;;             activity (mock/there-is-an-activity)
 ;;             like (actions.like/like-activity activity user)
 ;;             request {:action action
 ;;                      :params {:id (str (:_id like))}}
 ;;             response (filter-action action request)]
 ;;         (let [response (apply-view request response)]
 ;;           response => map?
 ;;           (:status response) => status/redirect?
 ;;           (get-in response [:headers "Location"]) => "/admin/likes")))))
)
