(ns jiksnu.modules.web.filters.activity-filters-test
  (:use [clj-factory.core :only [factory]]
        [ciste.core :only [with-serialization with-format
                           *serialization* *format*]]
        [ciste.filters :only [filter-action]]
        [jiksnu.test-helper :only [check context future-context test-environment-fixture]]
        [jiksnu.modules.web.routes :only [app]]
        [midje.sweet :only [=>]])
  (:require [clj-tigase.element :as element]
            [clojure.tools.logging :as log]
            [jiksnu.namespace :as ns]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.mock :as mock]
            [jiksnu.model :as model]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.user :as model.user]))

(test-environment-fixture

 (context "filter-action #'actions.activity/oembed"
   (let [action #'actions.activity/oembed]
     (context "when the serialization is :http"
       (with-serialization :http
         (let [request {:params {:url .url. :format .format.}}]
           (filter-action action request) => .oembed-map.
           (provided
             (model.activity/fetch-by-remote-id .url.) => .activity.
             (actions.activity/oembed .activity.) => .oembed-map.))))))
 )
