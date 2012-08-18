(ns jiksnu.views.admin.group-views-test
  (:use [ciste.core :only [with-serialization with-format]]
        [ciste.filters :only [filter-action]]
        [ciste.views :only [apply-view]]
        [clj-factory.core :only [factory]]
        [jiksnu.ko :only [*dynamic*]]
        [jiksnu.test-helper :only [test-environment-fixture]]
        [midje.sweet :only [every-checker fact future-fact => contains]])
  (:require [clojure.tools.logging :as log]
            [hiccup.core :as h]
            [jiksnu.actions.admin.group-actions :as actions.admin.group]
            [jiksnu.model :as model]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.group :as model.group]
            [jiksnu.model.user :as model.user]
            jiksnu.views.stream-views)
  (:import org.apache.abdera2.model.Entry))

(test-environment-fixture

 (fact "apply-view #'actions.admin.group/index"
   (let [action #'actions.admin.group/index]
     (fact "when the serialization is :http"
       (with-serialization :http
         (fact "when the format is :html"
           (with-format :html
             (binding [*dynamic* false]
               (fact "when there are groups"
                 (model/drop-all!)
                 (let [groups (doall (map (fn [n]
                                            (model.group/create (factory :group)))
                                          (range 15)))
                       request {:action action}
                       response (filter-action action request)]
                   (apply-view request response) =>
                   (every-checker
                    map?
                    (fn [response]
                      (let [body (h/html (:body response))]
                        (fact
                          body => #"groups"
                          (doseq [group groups]
                            (let [pattern (re-pattern (str (:_id group)))]
                              body => pattern)))))))))))))))
 )
