(ns jiksnu.actions.resource-actions-test
  (:require [clj-factory.core :refer [factory fseq]]
            [jiksnu.actions.resource-actions :as actions.resource]
            [jiksnu.db :as db]
            [jiksnu.test-helper :as th]
            [midje.sweet :refer :all]
            [org.httpkit.client :as client])
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
      (actions.resource/update-record resource) => resource
      (provided
        (client/get test-url anything anything) => true))))
