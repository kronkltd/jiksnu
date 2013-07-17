(ns jiksnu.model.feed-source-test
  (:use [clj-factory.core :only [factory]]
        [jiksnu.test-helper :only [check context future-context test-environment-fixture]]
        [jiksnu.session :only [with-user]]
        [jiksnu.model.feed-source :only [create create-validators count-records
                                         delete drop! fetch-all fetch-by-id]]
        [midje.sweet :only [=> anything throws]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.actions.feed-source-actions :as actions.feed-source]
            [jiksnu.mock :as mock]
            [jiksnu.model :as model]
            [jiksnu.model.feed-source :as model.feed-source]
            [jiksnu.util :as util])
  (:import jiksnu.model.FeedSource
           org.bson.types.ObjectId
           org.joda.time.DateTime
           slingshot.ExceptionInfo))

(test-environment-fixture

 (context #'fetch-by-id
   (let [source (mock/a-feed-source-exists)]
     (fetch-by-id (:_id source)) => source))

 (context "create"
   (future-context "when given valid parameters"
     (create {:_id (util/make-id)}) =>
     (check [response]
       response => (partial instance? FeedSource)
       (:_id response) => (partial instance? ObjectId)
       (:created response) => (partial instance? DateTime)
       (:topic response) => string?)
     (provided
       (create-validators anything) => []))

   (context "when given invalid parameters"
     (create .params.) => (throws RuntimeException)
     (provided
       (create-validators .params.) => [.error.])))

 (context #'delete
   (let [source (mock/a-feed-source-exists)]
     (delete source) => source
     (fetch-by-id (:_id source)) => nil?))

 (context #'fetch-all
   (fetch-all) => seq?)

 (context #'count-records
   (context "when there aren't any items"
     (drop!)
     (count-records) => 0)
   (context "when there are items"
     (drop!)
     (let [n 15]
       (dotimes [i n]
         (mock/a-feed-source-exists))
       (count-records) => n)))

 )
