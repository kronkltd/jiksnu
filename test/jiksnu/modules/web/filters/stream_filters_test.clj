(ns jiksnu.modules.web.filters.stream-filters-test
  (:require [ciste.core :refer [with-format with-serialization]]
            [ciste.filters :refer [filter-action]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.stream-actions :as actions.stream]
            [jiksnu.db :as db]
            [jiksnu.mock :as mock]
            [jiksnu.test-helper :refer [test-environment-fixture]]
            [lamina.core :refer [channel]]
            [midje.sweet :refer [=> fact]]))

(test-environment-fixture

 (fact "filter-action #'actions.stream/public-timeline"
   (let [action #'actions.stream/public-timeline]

     (fact "when the serialization is :http"
       (with-serialization :http

         (fact "when the format is :html"
           (with-format :html

             (fact "when there are no activities"
               (db/drop-all!)
               (let [request {:action action}]
                 (let [response (filter-action action request)]
                   response => map?)))
             ))
         ))
     ))

 (fact "filter-action #'actions.stream/user-timeline"
   (let [action #'actions.stream/user-timeline]
     (fact "when the serialization is :http"
       (with-serialization :http
         (fact "when the user exists"
           (let [user (mock/a-user-exists)
                 request {:params {:id (str (:_id user))}}]
             (filter-action action request) => .response.
             (provided
              (actions.stream/user-timeline user) => .response.)))))))

 )
