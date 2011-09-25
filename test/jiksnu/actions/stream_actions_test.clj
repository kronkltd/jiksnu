(deftest user-timeline-test)

(deftest friends-timeline-test)

(deftest inbox-test)

(deftest index-test
  (testing "when there are no activities"
    (fact "should be empty"
      (model.activity/drop!)
      (index) => empty?))
  (testing "when there are activities"
    (fact "should return a seq of activities"
      (let [author (model.user/create (factory User))]
        (with-user author
          (model.activity/create (factory Activity))))
      (let [response (index)]
        response => seq?
        response => (partial every? activity?)))))

(deftest test-remote-profile
  (fact
    (remote-profile) => nil))

(deftest test-show
  (testing "when the user exists"
    (facts "should return that user"
      (let [user (model.user/create (factory User))
            response (show user)]
        response => (partial instance? User)
        response => user))))

(deftest test-remote-user
  (fact
    (remote-user user) => user?))

