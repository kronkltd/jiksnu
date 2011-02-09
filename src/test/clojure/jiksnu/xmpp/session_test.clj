(ns jiksnu.xmpp.session-test
  (:use jiksnu.mock
        [lazytest.describe :only (describe it testing given for-any)]
        jiksnu.xmpp.session)
  (:require [lazytest.random :as r])
  (:import tigase.xmpp.BareJID
           tigase.xmpp.JID))

(describe session)

(describe with-session-binding)

(describe get-jid)

(describe is-user?
  (testing "when the user is logged in"
    (it "should be true"
      (for-any [user1 (r/string-of (r/pick r/alphanumeric))]
        (given [jid1 (JID/jidInstance user1 "example.com" "foo")
                bare-jid1 (BareJID/bareJIDInstance user1 "example.com")]
          (with-session-binding
            (mock-resource-connection :authorized true :jid jid1)
            (true? (is-user? bare-jid1)))))))
  (testing "when a different user is logged in"
    (it "should be false"
      (for-any [user1 (r/string-of (r/pick r/alphanumeric))
                user2 (r/string-of (r/pick r/alphanumeric))]
        (given [jid2 (JID/jidInstance user2 "example.com" "foo")
                bare-jid1 (BareJID/bareJIDInstance user1 "example.com")]
          (with-session-binding
            (mock-resource-connection :authorized true :jid jid2)
            (false? (is-user? bare-jid1)))))))
  (testing "when no user is logged in"
    (it "should be false"
      (for-any [user1 (r/string-of (r/pick r/alphanumeric))]
        (given [bare-jid1 (BareJID/bareJIDInstance user1 "example.com")]
          (with-session-binding
            (mock-resource-connection :authorized false)
            (false? (is-user? bare-jid1))))))))

(describe current-user)
