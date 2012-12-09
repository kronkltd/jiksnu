(ns jiksnu.model.feed-source-test
  (:use [clj-factory.core :only [factory]]
        [jiksnu.test-helper :only [test-environment-fixture]]
        [jiksnu.session :only [with-user]]
        [jiksnu.model.feed-source :only [create create-validators count-records
                                         delete drop! fetch-all fetch-by-id]]
        [midje.sweet :only [=> anything every-checker fact future-fact throws]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.actions.feed-source-actions :as actions.feed-source]
            [jiksnu.existance-helpers :as existance]
            [jiksnu.model :as model]
            [jiksnu.model.feed-source :as model.feed-source]
            [jiksnu.util :as util])
  (:import jiksnu.model.FeedSource
           org.bson.types.ObjectId
           org.joda.time.DateTime
           slingshot.ExceptionInfo))

(test-environment-fixture

 (fact "#'fetch-by-id"
   (let [source (existance/a-feed-source-exists)]
     (fetch-by-id (:_id source)) => source))

 (fact "create"
   (future-fact "when given valid parameters"
     (create {:_id (util/make-id)}) =>
     (every-checker
      (partial instance? FeedSource)
      #(instance? ObjectId (:_id %))
      #(instance? DateTime (:created %))
      #(string? (:topic %)))
     (provided
       (create-validators anything) => []))

   (fact "when given invalid parameters"
     (create .params.) => (throws RuntimeException)
     (provided
       (create-validators .params.) => [.error.])))

 (fact "#'delete"
   (let [source (existance/a-feed-source-exists)]
     (delete source) => source
     (fetch-by-id (:_id source)) => nil?))

 (fact "#'fetch-all"
   (fetch-all) => seq?)

 (fact "#'count-records"
   (fact "when there aren't any items"
     (drop!)
     (count-records) => 0)
   (fact "when there are items"
     (drop!)
     (let [n 15]
       (dotimes [i n]
         (existance/a-feed-source-exists))
       (count-records) => n)))

 )
