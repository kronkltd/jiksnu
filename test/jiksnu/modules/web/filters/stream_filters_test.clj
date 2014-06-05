(ns jiksnu.modules.web.filters.stream-filters-test
  (:require [ciste.core :refer [with-format with-serialization]]
            [ciste.filters :refer [filter-action]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.stream-actions :as actions.stream]
            [jiksnu.db :as db]
            [jiksnu.mock :as mock]
            [jiksnu.test-helper :refer [check context future-context test-environment-fixture]]
            [lamina.core :refer [channel]]
            [midje.sweet :refer [=>]]))

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
