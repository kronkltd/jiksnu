(ns jiksnu.model.user-test
  (:use ciste.factory
        jiksnu.model
        jiksnu.model.user
        [lazytest.describe :only (describe do-it testing)]
        [lazytest.expect :only (expect)])
  (:import jiksnu.model.User))

(describe get-id)

(describe get-domain)

(describe bare-jid)

(describe split-uri)

(describe rel-filter)

(describe rel-filter-feed)

(describe get-link)

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

(describe fetch-by-id)

(describe fetch-by-jid)

(describe fetch-by-uri)

(describe find-or-create)

(describe find-or-create-by-uri)

(describe find-or-create-by-jid)

(describe subnodes)

(describe edit
  (testing "when the user is found"
    (do-it "should return a user" :pending))
  (testing "when the user is not found"
    (do-it "should return nil" :pending)))

(describe delete
  (testing "when the user exists"
    (do-it "should be deleted" :pending)))

(describe add-node)

(describe inbox)

(describe update
  (testing "when the request is valid"
    (do-it "should return a user"
      (let [request {:params {"id" (fseq :word)}}]))))

(describe local?)

(describe get-uri)

(describe author-uri)

(describe get-domain)

(describe user-meta-uri)

(describe rule-element?)
