(ns jiksnu.modules.web.filters.stream-filters-test
  (:use [clj-factory.core :only [factory fseq]]
        [ciste.config :only [config]]
        [ciste.core :only [with-format with-serialization]]
        [ciste.filters :only [filter-action]]
        [jiksnu.test-helper :only [check context future-context test-environment-fixture]]
        [lamina.core :only [channel]]
        [midje.sweet :only [=>]])
  (:require [clj-tigase.core :as tigase]
            [clj-tigase.packet :as packet]
            [clojure.tools.logging :as log]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.actions.stream-actions :as actions.stream]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.db :as db]
            [jiksnu.mock :as mock]
            [jiksnu.model :as model]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.user :as model.user])
  (:import jiksnu.model.Conversation
           jiksnu.model.User))

(test-environment-fixture

 (context "filter-action #'actions.stream/public-timeline"
   (let [action #'actions.stream/public-timeline]

     (context "when the serialization is :http"
       (with-serialization :http

         (context "when the format is :html"
           (with-format :html

             (context "when there are no activities"
               (db/drop-all!)
               (let [request {:action action}]
                 (filter-action action request) =>
                 (check [response]
                   response => map?)))
             ))
         ))
     ))

 (context "filter-action #'actions.stream/user-timeline"
   (let [action #'actions.stream/user-timeline]
     (context "when the serialization is :http"
       (with-serialization :http
         (context "when the user exists"
           (let [user (mock/a-user-exists)
                 request {:params {:id (str (:_id user))}}]
             (filter-action action request) => .response.
             (provided
              (actions.stream/user-timeline user) => .response.)))))))

 )
