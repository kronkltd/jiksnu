(ns jiksnu.atom.view.activity-view-test
  (:use ciste.view
        jiksnu.atom.view.activity-view
        [lazytest.describe :only (describe it testing given do-it)]
        [lazytest.expect :only (expect)]
        jiksnu.factory
        jiksnu.mock
        jiksnu.model
        jiksnu.view)
  (:require [jiksnu.model.user :as model.user])
  (:import org.apache.abdera.model.Entry
           jiksnu.model.Activity
           jiksnu.model.User))

(describe has-author?)

(describe set-actor)

(describe parse-json-element
  (testing "when there are attributes"
    (do-it "should have an attribute"
      (let [json-map (extension-with-attributes-map)
            response (parse-json-element json-map)]
        (expect (seq (.getAttributes response))))))
  (testing "when there are children elements"
    (do-it "should add those elements"
      (let [response (parse-json-element (extension-with-children-map))]
        (expect (seq (.getElements response)))))))

(describe new-entry)

(describe add-extensions)

(describe add-author)

(describe add-authors)

(describe to-json
  (do-it "should not be nil"
    (let [entry (mock-activity-entry)
          response (to-json entry)]
      (expect (not (nil? response))))))

(describe to-activity
  (do-it "should return a map"
    (let [entry (mock-activity-entry)
          response (to-activity entry)]
      (expect (map? response)))))

(describe to-entry
  (do-it "should return an abdera entry"
    (with-serialization :http
      (with-format :atom
        (with-environment :test
          (let [user (factory User)
                actor (model.user/create user)
                activity (factory Activity {:authors [(:_id actor)]})
                response (to-entry activity)]
            (expect (instance? Entry response))
            (expect (.getId response))
            (expect (.getTitle response))
            (expect (.getUpdated response))))))))
