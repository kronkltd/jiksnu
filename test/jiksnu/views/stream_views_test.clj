(ns jiksnu.views.stream-views-test
  (:use [ciste.core :only [with-context with-serialization with-format
                           *serialization* *format*]]
        [ciste.filters :only [filter-action]]
        [ciste.views :only [apply-view]]
        [clj-factory.core :only [factory]]
        [jiksnu.session :only [with-user]]
        [jiksnu.test-helper :only [test-environment-fixture]]
        [jiksnu.actions.stream-actions :only [public-timeline user-timeline]]
        [midje.sweet :only [every-checker fact future-fact => contains truthy]])
  (:require [clojure.string :as string]
            [clojure.tools.logging :as log]
            [hiccup.core :as h]
            [jiksnu.abdera :as abdera]
            [jiksnu.model :as model]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.user :as model.user]
            jiksnu.views.stream-views)
  (:import org.apache.abdera2.model.Entry))

(test-environment-fixture

 (fact "apply-view #'public-timeline"
   (let [action #'public-timeline]
     (fact "when the serialization is :http"
       (with-serialization :http
         (fact "when the format is :atom"
           (with-format :atom
             (fact "when there are activities"
               (model/drop-all!)
               (let [user (model.user/create (factory :local-user))]
                 (dotimes [n 25]
                   (model.activity/create (factory :activity
                                                   {:author (:_id user)}))))

               (let [request {:action action}
                     response (filter-action action request)]

                 (apply-view request response) =>
                 (every-checker
                  map?
                  #(not (:template %))
                  (fn [response]
                    (let [feed (abdera/parse-xml-string (:body response))]
                      (fact
                        (count (.getEntries feed)) => 20))))))))
         
         (fact "when the format is :html"
           (with-format :html
             (fact "when there are activities"
               (model/drop-all!)
               (let [user (model.user/create (factory :local-user))]
                 (dotimes [n 25]
                   (model.activity/create (factory :activity
                                                   {:author (:_id user)}))))

               (let [request {:action action}
                     response (filter-action action request)]
                 (apply-view request response) =>
                 (every-checker
                  map?
                  (fn [response]
                    (let [body (h/html (:body response))]
                      (fact
                        body => #"20"
                        body => string?))))))))))))

 (fact "apply-view #'user-timeline"
   (let [action #'user-timeline]
     (fact "when the serialization is :http"
       (with-serialization :http

         (fact "when the format is :html"
           (with-format :html
             (fact "when that user has activities"
               (model/drop-all!)
               (let [user (model.user/create (factory :local-user))
                     activity (model.activity/create (factory :activity {:author (:_id user)}))
                     request {:action action
                              :params {:id (str (:_id user))}}
                     response (filter-action action request)]
                 (apply-view request response) =>
                 (every-checker
                  (fn [response]
                    (let [body (h/html (:body response))]
                      (fact
                        body => (re-pattern (str ".*activity-" (:_id activity) ".*"))))))))))
         
         (fact "when the format is :n3"
           (with-format :n3
             (fact "when that user has activities"
               (model/drop-all!)
               (let [user (model.user/create (factory :local-user))
                     activity (model.activity/create (factory :activity {:author (:_id user)}))
                     request {:action action
                              :params {:id (str (:_id user))}}
                     response (filter-action action request)]
                 (apply-view request response) =>
                 (every-checker
                  map?
                  (fn [response]
                    (fact
                      (let [body (:body response)]
                        body => (partial every? vector?)
                        (let [m (model/triples->model body)]
                          m => truthy)))))))))))))
 
 )
