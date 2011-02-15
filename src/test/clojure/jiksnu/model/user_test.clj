(ns jiksnu.model.user-test
  (:use jiksnu.factory
        jiksnu.model
        jiksnu.model.user
        [lazytest.describe :only (describe it testing given)])
  (:import jiksnu.model.User))

(describe drop!)

(describe create)

(describe index
  (testing "when there are no users"
    (it "should be empty"
      (with-environment :test
        (drop!)
        (let [response (index)]
          (empty? response)))))
  (testing "when there are users"
    (it "should not be empty"
      (with-environment :test
        (create (factory User))
        (let [response (index)]
          (seq response))))
    (it "should return a seq of users"
      (with-environment :test
        (create (factory User))
        (let [response (index)]
          (every? (partial instance? User) response))))))

(describe show
  (given [id (fseq :word)]
    (testing "when the user is found"
      (it "should return a user"
        (with-environment :test
          (create (factory User {:_id id}))
          (let [response (show id)]
            (instance? User response)))))
    (testing "when the user is not found"
      (it "should return nil"
        (with-environment :test
          (drop!)
          (let [response (show id)]
            (nil? response)))))))

(describe edit
  (testing "when the user is found"
    (it "should return a user" :pending))
  (testing "when the user is not found"
    (it "should return nil" :pending)))

(describe delete
  (testing "when the user exists"
    (it "should be deleted" :pending)))

(describe bare-jid)

(describe get-id)

(describe get-domain)

(describe subnodes)

(describe add-node)

(describe following)

(describe followers)

(describe update
  (testing "when the request is valid"
    (given [request {:params {"id" (fseq :word)}}]
      (it "should return a user" :pending))))
