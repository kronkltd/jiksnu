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
            [jiksnu.db :as db]
            [jiksnu.mock :as mock]
            [jiksnu.features-helper :as feature]
            [jiksnu.model :as model]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.feed-source :as model.feed-source]
            [jiksnu.model.user :as model.user])
  (:import jiksnu.model.Conversation))

(test-environment-fixture

 (fact "#'public-timeline"
   (fact "when there are no activities"
     (fact "should be empty"
       (model.activity/drop!)
       (public-timeline) => (comp empty? :items)))
   (fact "when there are activities"
     (fact "should return a seq of activities"
       (let [activity (mock/there-is-an-activity)]
         (public-timeline) =>
         (every-checker
          (fn [response]
            (fact
              response => map?
              (:total-records response) => 1
              (let [items (:items response)]
                items => seq?
                (doseq [item items]
                  (class item) => Conversation)))))))))

 (fact "#'user-timeline"
   (fact "when the user has activities"
     (db/drop-all!)
     (let [user (mock/a-user-exists)
           activity (mock/there-is-an-activity)]
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
       (let [user (mock/a-user-exists)
             source (mock/a-feed-source-exists)
             activity (factory :activity {:id (fseq :uri)})
             feed (abdera/make-feed* {:links
                                      [{:rel "self"
                                        :href (:topic source)}]
                                      :entries (index-section [activity])})]
         (actions.feed-source/add-watcher source user)
         activity => model/activity?
         (callback-publish feed)
         (model.activity/fetch-by-remote-id (:id activity)) => truthy))))

 )

