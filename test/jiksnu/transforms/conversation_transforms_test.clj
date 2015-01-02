(ns jiksnu.transforms.conversation-transforms-test
  (:require [clj-factory.core :refer [factory]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.actions.feed-source-actions :as actions.feed-source]
            [jiksnu.factory :refer [make-uri]]
            [jiksnu.mock :as mock]
            [jiksnu.model :as model]
            [jiksnu.test-helper :refer [test-environment-fixture]]
            [jiksnu.transforms.conversation-transforms :refer [set-update-source]]
            [jiksnu.util :as util]
            [midje.sweet :refer [=> anything contains fact]]))

(test-environment-fixture

 (fact #'set-update-source
   (let [source (mock/a-feed-source-exists)]

     (fact "when the update source is set"
       (let [conversation (factory :conversation {:update-source (:_id source)})]
         (set-update-source conversation) => conversation))

     (fact "when the update source is not set"
       (fact "and the source can be discovered"
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
