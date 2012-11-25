(ns jiksnu.model.activity-test
  (:use [clj-factory.core :only [factory fseq]]
        [jiksnu.test-helper :only [test-environment-fixture]]
        [jiksnu.session :only [with-user]]
        [jiksnu.model.activity :only [create create-validators count-records drop!
                                      get-author]]
        [midje.sweet :only [anything fact future-fact =>]]
        [validateur.validation :only [valid?]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.existance-helpers :as existance]
            [jiksnu.factory :as factory]
            [jiksnu.features-helper :as feature]
            [jiksnu.model :as model]
            [jiksnu.model.user :as model.user])
  (:import jiksnu.model.Activity
           jiksnu.model.User))
 
(test-environment-fixture

 (fact "#'create"
   (let [domain (existance/a-domain-exists)
         feed-source (existance/a-feed-source-exists)
         conversation (existance/a-conversation-exists)
         id (factory/make-uri (:_id domain) (fseq :path))
         activity (actions.activity/prepare-create
                   (factory :activity {:conversation (:_id conversation)
                                       :id id
                                       :update-source (:_id feed-source)}))]
     (create activity) => model/activity?))
 
 (fact "#'get-author"
   (let [user (existance/a-user-exists)
         activity (existance/there-is-an-activity {:user user})]
     (get-author activity) => user))

 (fact "#'count-records"
   (fact "when there aren't any items"
     (drop!)
     (count-records) => 0)
   (fact "when there are conversations"
     (drop!)
     (let [n 15]
       (dotimes [i n]
         (existance/there-is-an-activity))
       (count-records) => n)))


 )
