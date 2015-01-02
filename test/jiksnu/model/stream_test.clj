(ns jiksnu.model.stream-test
  (:use [clj-factory.core :only [factory fseq]]
        [jiksnu.test-helper :only [context test-environment-fixture]]
        [midje.sweet :only [=> fact]]
        [validateur.validation :only [valid?]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.actions.stream-actions :as actions.stream]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.factory :as factory]
            [jiksnu.mock :as mock]
            [jiksnu.model :as model]
            [jiksnu.model.stream :as model.stream]
            [jiksnu.model.user :as model.user]
            [jiksnu.session :as session])
  (:import jiksnu.model.Activity
           jiksnu.model.User))

(test-environment-fixture

 (fact #'model.stream/count-records

   (fact "when there aren't any items"
     (model.stream/drop!)
     (model.stream/count-records) => 0)

   (fact "when there are items"
     (model.stream/drop!)
     (let [n 15]
       (dotimes [i n]
         (mock/a-stream-exists))
       (model.stream/count-records) => n))
   )

 ;; (fact #'create
 ;;   (let [domain (mock/a-domain-exists)
 ;;         feed-source (mock/a-feed-source-exists {:domain domain})
 ;;         conversation (mock/a-conversation-exists {:feed-source feed-source})
 ;;         id (factory/make-uri (:_id domain) (fseq :path))
 ;;         activity (actions.activity/prepare-create
 ;;                   (factory :activity {:conversation (:_id conversation)
 ;;                                       :id id
 ;;                                       :local false
 ;;                                       :update-source (:_id feed-source)}))]
 ;;     (create activity) => (partial instance? Activity)))

 )
