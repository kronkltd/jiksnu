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

;; (fact #'count-records
;;   (fact "when there aren't any items"
;;     (drop!)
;;     (count-records) => 0)
;;   (fact "when there are items"
;;     (drop!)
;;     (let [n 15]
;;       (dotimes [i n]
;;         (mock/a-feed-source-exists))
;;       (count-records) => n)))

;; (fact #'delete
;;   (let [source (mock/a-feed-source-exists)]
;;     (delete source) => source
;;     (fetch-by-id (:_id source)) => nil?))

;; (fact #'fetch-by-id
;;   (let [source (mock/a-feed-source-exists)]
;;     (fetch-by-id (:_id source)) => source))

;; (fact #'create
;;   (future-fact "when given valid parameters"
;;     (create {:_id (util/make-id)}) =>
;;     (check [response]
;;       response => (partial instance? FeedSource)
;;       (:_id response) => (partial instance? ObjectId)
;;       (:created response) => (partial instance? DateTime)
;;       (:topic response) => string?)
;;     (provided
;;       (create-validators anything) => []))

;;   (fact "when given invalid parameters"
;;     (create .params.) => (throws RuntimeException)
;;     (provided
;;       (create-validators .params.) => [.error.])))

;; (fact #'fetch-all
;;   (fetch-all) => seq?)

(fact #'model.feed-source/find-by-user

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

