(ns jiksnu.helpers.activity-helpers-test
  (:use ciste.core
        ciste.factory
        ciste.sections
        ciste.sections.default
        ;; ciste.views
        clj-tigase.core
        jiksnu.helpers.activity-helpers
        jiksnu.model
        jiksnu.namespace
        jiksnu.session
        jiksnu.view
        [lazytest.describe :only (describe testing do-it for-any)]
        [lazytest.expect :only (expect)])
  (:import jiksnu.model.Activity))


(describe new-entry)

(describe add-author)

(describe add-authors)

(describe add-entry)

(describe get-actor)

(describe privacy-section)

(describe activity-form)

(describe delete-link)

(describe edit-link)

(describe comment-link)

(describe like-link)

(describe update-button)

(describe make-feed)

(describe comment-link-item)

(describe acl-link)

(describe parse-extension-element)

(describe get-authors)

(describe to-activity
  (do-it "should return a map"
    (with-serialization :http
      (with-format :atom
        (let [activity (factory Activity)
              entry (show-section activity)
              response (to-activity entry)]
          (expect (map? response)))))))

(describe to-json
  (do-it "should not be nil"
    (with-serialization :http
      (with-format :atom
        (let [activity (factory Activity)
              entry (show-section activity)
              response (to-json entry)]
          (expect (not (nil? response))))))))

;; (describe parse-json-element
;;   (testing "when there are attributes"
;;     (do-it "should have an attribute"
;;       (let [json-map (extension-with-attributes-map)
;;             response (parse-json-element json-map)]
;;         (expect (seq (.getAttributes response))))))
;;   (testing "when there are children elements"
;;     (do-it "should add those elements"
;;       (let [response (parse-json-element (extension-with-children-map))]
;;         (expect (seq (.getElements response)))))))

(describe add-extensions)

(describe has-author?)

(describe comment-node-uri)

(describe comment-request)
