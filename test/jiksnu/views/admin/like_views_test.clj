(ns jiksnu.views.admin.like-views-test
  (:use [ciste.core :only [with-serialization with-format]]
        [ciste.filters :only [filter-action]]
        [ciste.views :only [apply-view]]
        [clj-factory.core :only [factory]]
        [jiksnu.test-helper :only [check context future-context test-environment-fixture]]
        [midje.sweet :only [=>]])
  (:require [clojure.tools.logging :as log]
            [clojurewerkz.support.http.statuses :as status]
            [hiccup.core :as h]
            [jiksnu.actions.admin.like-actions :as actions.admin.like]
            [jiksnu.actions.like-actions :as actions.like]
            [jiksnu.mock :as mock]
            [jiksnu.features-helper :as feature]
            [jiksnu.model :as model]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.feed-subscription :as model.feed-subscription]
            [jiksnu.model.like :as model.like]))

(test-environment-fixture

 (context "apply-view #'actions.admin.like/delete"
   (let [action #'actions.admin.like/delete]
     (context "when the serialization is :http"
       (with-serialization :http
         (context "when the format is :html"
           (with-format :html
             (let [user (mock/a-user-exists)
                   activity (mock/there-is-an-activity)
                   like (actions.like/like-activity activity user)
                   request {:action action
                            :params {:id (str (:_id like))}}
                   response (filter-action action request)]
               (apply-view request response) =>
               (check [response]
                response => map?
                (:status response) => status/redirect?
                (get-in response [:headers "Location"]) => "/admin/likes")))))))))
