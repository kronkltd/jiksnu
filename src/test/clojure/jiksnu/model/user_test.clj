(ns jiksnu.model.user-test
  (:use jiksnu.factory
        jiksnu.model
        jiksnu.model.user
        [lazytest.describe :only (describe do-it testing)]
        [lazytest.expect :only (expect)])
  (:import jiksnu.model.User))

(describe drop!)

(describe create)

(describe index
  (testing "when there are no users"
    (do-it "should be empty"
      (drop!)
      (let [response (index)]
        (expect (empty? response)))))
  (testing "when there are users"
    (do-it "should not be empty"
      (create (factory User))
      (let [response (index)]
        (expect (seq response))))
    (do-it "should return a seq of users"
      (create (factory User))
      (let [response (index)]
        (expect (every? (partial instance? User) response))))))

(describe show
  (testing "when the user is found"
    (do-it "should return a user"
      (let [username (fseq :id)]
        (create (factory User {:username username}))
        (let [response (show username)]
          (expect (instance? User response))))))
  (testing "when the user is not found"
    (do-it "should return nil"
      (drop!)
      (let [username (fseq :id)]
        (let [response (show username)]
          (expect (expect (nil? response))))))))

(describe edit
  (testing "when the user is found"
    (do-it "should return a user" :pending))
  (testing "when the user is not found"
    (do-it "should return nil" :pending)))

(describe delete
  (testing "when the user exists"
    (do-it "should be deleted" :pending)))

(describe bare-jid)

(describe get-id)

(describe get-domain)

(describe subnodes)

(describe add-node)

(describe following)

(describe followers)

(describe update
  (testing "when the request is valid"
    (do-it "should return a user"
      (let [request {:params {"id" (fseq :word)}}]))))
