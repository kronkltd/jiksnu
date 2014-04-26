(ns jiksnu.transforms.conversation-transforms-test
  (:use [clj-factory.core :only [factory]]
        [jiksnu.transforms.conversation-transforms :only [set-update-source]]
        [jiksnu.factory :only [make-uri]]
        [jiksnu.test-helper :only [context test-environment-fixture]]
        [midje.sweet :only [=> contains anything]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.actions.feed-source-actions :as actions.feed-source]
            [jiksnu.mock :as mock]
            [jiksnu.model :as model]
            [jiksnu.util :as util]))

(test-environment-fixture

 (context #'set-update-source
   (let [source (mock/a-feed-source-exists)]

     (context "when the update source is set"
       (let [conversation (factory :conversation {:update-source (:_id source)})]
         (set-update-source conversation) => conversation))

     (context "when the update source is not set"
       (context "and the source can be discovered"
         (let [params {:url (:topic source)
                       ;; This is set by the other transform
                       :_id (util/make-id)}
               conversation (-> (factory :conversation params)
                                (dissoc :update-source))
               url (:url conversation)]
           (set-update-source conversation) => (contains {:update-source .id.})
           (provided
             (actions.feed-source/discover-source anything) => {:_id .id.}))))
     ))
 )
