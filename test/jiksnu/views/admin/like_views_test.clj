(ns jiksnu.views.admin.like-views-test
  (:use [ciste.core :only [with-serialization with-format]]
        [ciste.filters :only [filter-action]]
        [ciste.views :only [apply-view]]
        [clj-factory.core :only [factory]]
        [jiksnu.test-helper :only [test-environment-fixture]]
        [midje.sweet :only [every-checker fact future-fact =>]])
  (:require [clojure.tools.logging :as log]
            [clojurewerkz.support.http.statuses :as status]
            [hiccup.core :as h]
            [jiksnu.actions.admin.like-actions :as actions.admin.like]
            [jiksnu.actions.like-actions :as actions.like]
            [jiksnu.features-helper :as feature]
            [jiksnu.model :as model]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.feed-subscription :as model.feed-subscription]
            [jiksnu.model.like :as model.like]))

(test-environment-fixture

 (fact "apply-view #'actions.admin.like/delete"
   (let [action #'actions.admin.like/delete]
     (fact "when the serialization is :http"
       (with-serialization :http
         (fact "when the format is :html"
           (with-format :html
             (let [user (feature/a-user-exists)
                   activity (feature/there-is-an-activity)
                   like (actions.like/like-activity activity user)
                   request {:action action
                            :params {:id (str (:_id like))}}
                   response (filter-action action request)]
               (apply-view request response) =>
               (every-checker
                map?
                (comp status/redirect? :status)
                #(= (get-in % [:headers "Location"])
                    "/admin/likes"))))))))))
