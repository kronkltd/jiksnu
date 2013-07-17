(ns jiksnu.filters.like-filters-test
  (:use [clj-factory.core :only [factory]]
        [ciste.core :only [with-serialization with-format
                           *serialization* *format*]]
        [ciste.filters :only [filter-action]]
        [jiksnu.test-helper :only [check context future-context test-environment-fixture]]
        [midje.sweet :only [=>]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.actions.like-actions :as actions.like]
            [jiksnu.model :as model]
            [jiksnu.model.like :as model.like]
            [jiksnu.util :as util]
            [ring.mock.request :as req]))

(test-environment-fixture

 (context "filter-action #'actions.like/delete"
   (let [action #'actions.like/delete]
     (context "when the serialization is :http"
       (with-serialization :http
         (let [request {:params {:id .id.}}]
           (filter-action action request) => .response.
           (provided
             (model.like/fetch-by-id (util/make-id .id.)) => .like.
             (actions.like/delete .like.) => .response.))))))

 )
