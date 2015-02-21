(ns jiksnu.actions.resource-actions-test
  (:require [clj-factory.core :refer [factory fseq]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.resource-actions :as actions.resource]
            [jiksnu.db :as db]
            [jiksnu.mock :as mock]
            [jiksnu.model :as model]
            [jiksnu.test-helper :as th]
            [jiksnu.util :as util]
            [midje.sweet :refer :all])
  (:import jiksnu.model.Resource
           org.joda.time.DateTime))

(namespace-state-changes
 [(before :contents (th/setup-testing))
  (after :contents (th/stop-testing))])

(def test-url "http://www.example.com/")

(facts "create"
  (let [params {:_id test-url}]
    (actions.resource/create params)) =>
    (every-checker
     #(instance? Resource %)
     (contains {:_id test-url
                :updated #(instance? DateTime %)
                :created #(instance? DateTime %)
                :local   #(instance? Boolean %)})))

(facts "update"
  (fact "when the resource exists"
    (db/drop-all!)
    (let [resource (actions.resource/create {:_id test-url})]
      (log/spy :info (actions.resource/update resource))) => truthy))
