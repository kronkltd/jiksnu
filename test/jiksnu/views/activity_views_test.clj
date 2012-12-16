(ns jiksnu.views.activity-views-test
  (:use [ciste.core :only [with-context with-serialization with-format
                           *serialization* *format*]]
        [ciste.filters :only [filter-action]]
        [ciste.sections.default :only [full-uri]]
        [ciste.views :only [apply-view]]
        [clj-factory.core :only [factory]]
        [jiksnu.session :only [with-user]]
        [jiksnu.test-helper :only [test-environment-fixture]]
        [jiksnu.actions.stream-actions :only [public-timeline user-timeline]]
        [midje.sweet :only [every-checker fact future-fact => contains]])
  (:require [clojure.string :as string]
            [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            [clojurewerkz.support.http.statuses :as status]
            [hiccup.core :as h]
            [jiksnu.abdera :as abdera]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.mock :as mock]
            [jiksnu.features-helper :as feature]
            [jiksnu.model :as model]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.user :as model.user]
            jiksnu.views.stream-views)
  (:import org.apache.abdera2.model.Entry))

(test-environment-fixture

 (fact "apply-view #'actions.activity/oembed"
   (let [action #'actions.activity/oembed]
     (fact "when the serialization is :http"
       (with-serialization :http
         (fact "when the format is :json"
           (with-format :json
             (let [activity (mock/there-is-an-activity)
                   request {:params {:url (:id activity)}
                            :action action}
                   response (filter-action action request)]
               (apply-view request response) =>
               (every-checker
                map?
                (comp status/success? :status)
                (fn [result]
                  (let [body (:body result)]
                    (fact
                      body => (contains
                               {:title (:title activity)}))))))))
         (fact "when the format is :xml"
           (with-format :xml
             (let [activity (mock/there-is-an-activity)
                   request {:params {:url (:id activity)}
                            :action action}
                   response (filter-action action request)]
               (apply-view request response) =>
               (every-checker
                map?
                :body
                (comp status/success? :status)
                (fn [result]
                  (let [body (:body result)]
                    (fact
                      body =not=> string?)))))))))))
 )
