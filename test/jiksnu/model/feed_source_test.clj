(ns jiksnu.model.feed-source-test
  (:use [clj-factory.core :only [factory]]
        [jiksnu.test-helper :only [test-environment-fixture]]
        [jiksnu.session :only [with-user]]
        [jiksnu.model.feed-source :only [create create-validators prepare
                                         fetch-by-id delete fetch-all]]
        [midje.sweet :only [anything fact => every-checker throws]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.model.feed-source :as model.feed-source])
  (:import jiksnu.model.FeedSource
           org.bson.types.ObjectId
           org.joda.time.DateTime
           slingshot.ExceptionInfo
           )

  )

(test-environment-fixture

 (fact "create"
   (fact "when given valid parameters"
     (create (factory :feed-source)) =>
     (every-checker
      (partial instance? FeedSource)
      #(instance? ObjectId (:_id %))
      #(instance? DateTime (:created %))
      #(string? (:topic %))))

   (fact "when given invalid parameters"
     (create .params.) => (throws RuntimeException)
     (provided
       (prepare .params.) => .prepared-params.
       (create-validators .prepared-params.) => [.error.])))

 (fact "#'delete"
   (let [source (create (factory :feed-source))]
     (delete source) => source
     (fetch-by-id (:_id source)) => nil?))

 (fact "#'fetch-all"
   (fetch-all) => seq?)

 )
