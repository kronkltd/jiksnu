(ns jiksnu.model.activity-test
  (:use [clj-factory.core :only [factory fseq]]
        [jiksnu.test-helper :only [context test-environment-fixture]]
        [jiksnu.model.activity :only [create create-validators count-records drop!
                                      get-author]]
        [midje.sweet :only [=>]]
        [validateur.validation :only [valid?]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.mock :as mock]
            [jiksnu.factory :as factory]
            [jiksnu.model :as model])
  (:import jiksnu.model.Activity
           jiksnu.model.User))

(test-environment-fixture

 (context #'count-records

   (context "when there aren't any items"
     (drop!)
     (count-records) => 0)

   (context "when there are conversations"
     (drop!)
     (let [n 15]
       (dotimes [i n]
         (mock/there-is-an-activity))
       (count-records) => n))
   )

 (context #'create
   (let [domain (mock/a-domain-exists)
         feed-source (mock/a-feed-source-exists {:domain domain})
         conversation (mock/a-conversation-exists {:feed-source feed-source})
         id (factory/make-uri (:_id domain) (fseq :path))
         activity (actions.activity/prepare-create
                   (factory :activity {:conversation (:_id conversation)
                                       :id id
                                       :local false
                                       :update-source (:_id feed-source)}))]
     (create activity) => (partial instance? Activity)))

 (context #'get-author

   (context "when given an empty activity"
     (let [item (Activity.)]
       (get-author item) => nil))

   (context "when given a real activity"
     (let [user (mock/a-user-exists)
           activity (mock/there-is-an-activity {:user user})]
       (get-author activity) => user))
   )

 )
