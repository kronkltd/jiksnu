(ns jiksnu.model.feed-source-test
  (:require [clj-factory.core :refer [factory]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.feed-source-actions :as actions.feed-source]
            [jiksnu.mock :as mock]
            [jiksnu.model :as model]
            [jiksnu.model.feed-source :as model.feed-source]
            [jiksnu.model.user :as model.user]
            [jiksnu.test-helper :as th]
            [jiksnu.util :as util]
            [midje.sweet :refer :all])
  (:import jiksnu.model.FeedSource
           org.bson.types.ObjectId
           org.joda.time.DateTime))

(namespace-state-changes
 [(before :contents (th/setup-testing))
  (after :contents (th/stop-testing))])

(facts "#'model.feed-source/count-records"
  (fact "when there aren't any items"
    (model.feed-source/drop!)
    (model.feed-source/count-records) => 0)
  (fact "when there are items"
    (model.feed-source/drop!)
    (let [n 15]
      (dotimes [i n]
        (mock/a-feed-source-exists))
      (model.feed-source/count-records) => n)))

(facts "#'delete"
  (let [source (mock/a-feed-source-exists)]
    (model.feed-source/delete source) => source
    (model.feed-source/fetch-by-id (:_id source)) => nil?))

(facts "#'fetch-by-id"
  (let [source (mock/a-feed-source-exists)]
    (model.feed-source/fetch-by-id (:_id source)) => source))

(facts "#'create"
  (future-fact "when given valid parameters"
    (model.feed-source/create {:_id (util/make-id)}) =>
    (every-checker
     (partial instance? FeedSource)
     (contains
      {:_id     (partial instance? ObjectId)
       :created (partial instance? DateTime)
       :topic   string?}))
    (provided
      (model.feed-source/create-validators anything) => []))

  (fact "when given invalid parameters"
    (model.feed-source/create .params.) => (throws RuntimeException)
    (provided
      (model.feed-source/create-validators .params.) => [.error.])))

(facts "#'fetch-all"
  (model.feed-source/fetch-all) => seq?)

(facts "#'model.feed-source/find-by-user"

  (fact "when the user is nil"
    (let [user nil]
      (model.feed-source/find-by-user user) => nil))

  (fact "when the user is not nil"
    (let [user (mock/a-user-exists)]

      (fact "and the user has a source"
        (let [source (mock/a-feed-source-exists)]
          (model.feed-source/find-by-user user) => nil))

      (fact "and the user does not have a source"
        (model.user/remove-field! user :update-source)
        (model.feed-source/find-by-user user) => nil)

      ))
  )

