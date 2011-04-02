(describe new-entry)

(describe add-author)

(describe add-authors)

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

(describe has-author?)

(describe set-actor)

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

(describe show-section "Activity :atom"
  (do-it "should return an abdera entry"
    (with-serialization :http
      (with-format :atom
        (let [user (factory User)
              actor (model.user/create user)
              activity (factory Activity {:authors [(:_id actor)]})
              response (show-section activity)]
          (expect (instance? Entry response))
          (expect (.getId response))
          (expect (.getTitle response))
          (expect (.getUpdated response)))))))

(describe uri "Activity"
  (do-it "should be a string"
    (with-serialization :http
      (with-format :html
        (with-user (model.user/create (factory User))
          (let [activity (model.activity/create (factory Activity))]
            (expect (string? (uri activity)))))))))

(describe add-form "Activity"
  (do-it "should be a vector"
    (with-serialization :http
      (with-format :html
        (with-user (model.user/create (factory User))
          (let [activity (model.activity/create (factory Activity))]
            (expect (vector? (add-form activity)))))))))

(describe show-section-minimal "[Activity :html]"
  (do-it "should be a vector"
    (with-serialization :http
      (with-format :html
        (with-user (model.user/create (factory User))
          (let [activity (model.activity/create (factory Activity))]
            (expect (vector? (show-section-minimal activity)))))))))

(describe edit-form
  (do-it "should be a vector"
    (with-serialization :http
      (with-format :html
        (with-user (model.user/create (factory User))
          (let [activity (model.activity/create (factory Activity))]
            (expect (vector? (edit-form activity)))))))))

(describe index-line-minimal
  (do-it "should be a vector"
    (with-serialization :http
      (with-format :html
        (with-user (model.user/create (factory User))
          (let [activity (model.activity/create (factory Activity))]
            (expect (vector? (index-line-minimal activity)))))))))

(describe index-block-minimal
  (do-it "should be a vector"
    (with-serialization :http
      (with-format :html
        (with-user (model.user/create (factory User))
          (let [activity (model.activity/create (factory Activity))]
            (expect (vector? (index-block-minimal [activity])))))))))

(describe show-section "Activity :xmpp :xmpp"
  (do-it "should return an element"
    (with-serialization :xmpp
      (with-format :xmpp
        (let [user (model.user/create (factory User))]
          (with-user user
            (let [entry (model.activity/create (factory Activity))
                  response (show-section entry)]
              (expect (not (nil? response)))
              (expect (element? response)))))))))
