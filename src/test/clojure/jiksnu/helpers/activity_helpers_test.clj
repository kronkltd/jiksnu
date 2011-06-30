(ns jiksnu.helpers.activity-helpers-test
  (:use ciste.core
        ciste.sections
        ciste.sections.default
        clj-factory.core
        clj-tigase.core
        jiksnu.helpers.activity-helpers
        jiksnu.model
        jiksnu.namespace
        jiksnu.session
        jiksnu.view
        [lazytest.deftest :only (deftest testing do-it for-any)]
        [lazytest.expect :only (expect)])
  (:import jiksnu.model.Activity))


(deftest new-entry)

(deftest add-author)

(deftest add-authors)

(deftest add-entry)

(deftest get-actor)

(deftest privacy-section)

(deftest activity-form)

(deftest delete-link)

(deftest edit-link)

(deftest comment-link)

(deftest like-link)

(deftest update-button)

(deftest make-feed)

(deftest comment-link-item)

(deftest acl-link)

(deftest parse-extension-element)

(deftest get-authors)

(deftest to-activity
  (do-it "should return a map"
    (with-serialization :http
      (with-format :atom
        (let [activity (factory Activity)
              entry (show-section activity)
              response (to-activity entry)]
          (expect (map? response)))))))

(deftest to-json
  (do-it "should not be nil"
    (with-serialization :http
      (with-format :atom
        (let [activity (factory Activity)
              entry (show-section activity)
              response (to-json entry)]
          (expect (not (nil? response))))))))

;; (deftest parse-json-element
;;   (testing "when there are attributes"
;;     (do-it "should have an attribute"
;;       (let [json-map (extension-with-attributes-map)
;;             response (parse-json-element json-map)]
;;         (expect (seq (.getAttributes response))))))
;;   (testing "when there are children elements"
;;     (do-it "should add those elements"
;;       (let [response (parse-json-element (extension-with-children-map))]
;;         (expect (seq (.getElements response)))))))

(deftest add-extensions)

(deftest has-author?)

(deftest comment-node-uri)

(deftest comment-request)

(deftest set-id
  (testing "when there is an id"
    (do-it "should not change the value"
      (let [activity (factory Activity)
            response (set-id activity)]
        (expect (= (:_id activity)
                   (:_id response))))))
  (testing "when there is no id"
    (do-it "should add an id key"
      (let [activity (factory Activity)
            response (set-id activity)]
        (:_id response)))))

(deftest set-object-id)

(deftest set-updated-time
  (testing "when there is an updated property"
    (do-it "should not change the value"
      (let [activity (factory Activity)
            response (set-updated-time activity)]
        (expect (= (:updated activity)
                   (:updated response))))))
  (testing "when there is no updated property"
    (do-it "should add an updated property"
      (let [activity (dissoc (factory Activity) :updated)
            response (set-updated-time activity)]
        (expect (:updated response))))))

(deftest set-object-updated)

(deftest set-published-time)

(deftest set-object-published)

(deftest set-actor)

(deftest set-public)

