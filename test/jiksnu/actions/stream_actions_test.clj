(ns jiksnu.actions.stream-actions-test
  (:require [ciste.core :refer [with-context]]
            [ciste.sections.default :refer [index-section]]
            [clj-factory.core :refer [factory fseq]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.feed-source-actions :as actions.feed-source]
            jiksnu.actions.stream-actions
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.db :as db]
            [jiksnu.mock :as mock]
            [jiksnu.model :as model]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.feed-source :as model.feed-source]
            [jiksnu.model.user :as model.user]
            [jiksnu.test-helper :refer [check context future-context test-environment-fixture]]
            [midje.sweet :refer [=> truthy]])
  (:import jiksnu.model.Activity
           jiksnu.model.Conversation))

(test-environment-fixture

 (context #'public-timeline
   (context "when there are no activities"
     (context "should be empty"
       (model.activity/drop!)
       (public-timeline) => (comp empty? :items)))
   (context "when there are activities"
     (context "should return a seq of activities"
       (let [activity (mock/there-is-an-activity)]
         (public-timeline) =>
         (check [response]
                response => map?
                (:totalRecords response) => 1
                (let [items (:items response)]
                  items => seq?
                  (doseq [item items]
                    (class item) => Conversation)))))))

 (context #'user-timeline
   (context "when the user has activities"
     (db/drop-all!)
     (let [user (mock/a-user-exists)
           activity (mock/there-is-an-activity)]
       (user-timeline user) =>
       (check [response]
              response => vector?
              (first response) => user
              (second response) => map?
              (:totalRecords (second response)) => 1))))

 ;; (future-context #'callback-publish
 ;;   (context "when there is a watched source"
 ;;     (with-context [:http :atom]
 ;;       (let [user (mock/a-user-exists)
 ;;             source (mock/a-feed-source-exists)
 ;;             activity (factory :activity {:id (fseq :uri)})
 ;;             feed (abdera/make-feed* {:links
 ;;                                      [{:rel "self"
 ;;                                        :href (:topic source)}]
 ;;                                      :entries (index-section [activity])})]
 ;;         (actions.feed-source/add-watcher source user)
 ;;         activity => (partial instance? Activity)
 ;;         (callback-publish feed)
 ;;         (model.activity/fetch-by-remote-id (:id activity)) => truthy))))

 )

