(ns jiksnu.modules.web.filters.activity-filters-test
  (:require [clj-factory.core :refer [factory]]
            [clojure.tools.logging :as log]
            [ciste.core :refer [with-serialization with-format
                               *serialization* *format*]]
            [ciste.filters :refer [filter-action]]
            [jiksnu.namespace :as ns]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.mock :as mock]
            [jiksnu.model :as model]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.user :as model.user]
            [jiksnu.modules.web.routes :refer [app]]
            [jiksnu.test-helper :refer [test-environment-fixture]]
            [midje.sweet :refer [=> fact]]))

(test-environment-fixture

 (fact "filter-action #'actions.activity/oembed"
   (let [action #'actions.activity/oembed]
     (fact "when the serialization is :http"
       (with-serialization :http
         (let [request {:params {:url .url. :format .format.}}]
           (filter-action action request) => .oembed-map.
           (provided
             (model.activity/fetch-by-remote-id .url.) => .activity.
             (actions.activity/oembed .activity.) => .oembed-map.))))))
 )
