(ns jiksnu.modules.core.actions.resource-actions-test
  (:require [clj-factory.core :refer [factory]]
            [jiksnu.modules.core.actions.resource-actions :as actions.resource]
            [jiksnu.modules.core.db :as db]
            [jiksnu.test-helper :as th]
            [midje.sweet :refer :all]
            [org.httpkit.client :as client])
  (:import jiksnu.modules.core.model.Resource
           org.joda.time.DateTime))

(def test-url "http://www.example.com/")

(th/module-test ["jiksnu.modules.core"])

(facts "create"
  (let [params {:_id test-url}]
    (actions.resource/create params) =>
    (every-checker
     #(instance? Resource %)
     (contains {:_id test-url
                :updated #(instance? DateTime %)
                :created #(instance? DateTime %)
                :local   #(instance? Boolean %)}))))

(facts "update"
  (fact "when the resource exists"
    (db/drop-all!)
    (let [resource (actions.resource/create {:_id test-url})]
      (actions.resource/update-record resource) => resource
      (provided
       (client/get test-url anything anything) => true))))
