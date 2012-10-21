(ns jiksnu.actions.stream-actions-test
  (:use [ciste.core :only [with-context]]
        [ciste.sections.default :only [index-section]]
        [clj-factory.core :only [factory fseq]]
        jiksnu.actions.stream-actions
        [jiksnu.test-helper :only [test-environment-fixture]]
        jiksnu.session
        midje.sweet)
  (:require [clojure.tools.logging :as log]
            [jiksnu.abdera :as abdera]
            [jiksnu.actions.feed-source-actions :as actions.feed-source]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.existance-helpers :as existance]
            [jiksnu.features-helper :as feature]
            [jiksnu.model :as model]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.feed-source :as model.feed-source]
            [jiksnu.model.user :as model.user]))

(test-environment-fixture

 (fact "#'public-timeline"
   (fact "when there are no activities"
     (fact "should be empty"
       (model.activity/drop!)
       (public-timeline) => (comp empty? :items)))
   (fact "when there are activities"
     (fact "should return a seq of activities"
       (let [activity (existance/there-is-an-activity)]
         (public-timeline) =>
         (every-checker
          map?
          #(seq? (:items %))
          #(= 1 (:total-records %))
          ;; #(every? activity? (first %))
          )))))

 (fact "#'user-timeline"
   (fact "when the user has activities"
     (model/drop-all!)
     (let [user (existance/a-user-exists)
           activity (existance/there-is-an-activity)]
       (user-timeline user) =>
       (every-checker
        vector?
        (fn [response]
          (fact
            (first response) => user
            (second response) => map?
            (:total-records (second response)) => 1))))))
 
 (future-fact "#'callback-publish"
   (fact "when there is a watched source"
     (with-context [:http :atom]
       (let [user (existance/a-user-exists)
             source (existance/a-feed-source-exists)
             activity (model/map->Activity
                       (factory :activity {:id (fseq :uri)}))
             feed (abdera/make-feed* {:links
                                      [{:rel "self"
                                        :href (:topic source)}]
                                      :entries (index-section [activity])})]
         (actions.feed-source/add-watcher source user)
         activity => model/activity?
         (callback-publish feed)
         (model.activity/fetch-by-remote-id (:id activity)) => truthy))))

 )

