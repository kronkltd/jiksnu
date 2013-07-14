(ns jiksnu.model.webfinger-test
  (:use [ciste.config :only [with-environment]]
        [clj-factory.core :only [factory fseq]]
        [midje.sweet :only [=> contains]]
        [jiksnu.test-helper :only [check context future-context test-environment-fixture]]
        jiksnu.model
        jiksnu.model.webfinger)
  (:require [ciste.model :as cm]
            [clojure.tools.logging :as log]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.mock :as mock]
            [jiksnu.namespace :as ns]
            [jiksnu.ops :as ops]
            [hiccup.core :as hiccup])
  (:import jiksnu.model.User
           nu.xom.Document))

(defn mock-xrd-with-username
  [username]
  (cm/string->document
   (hiccup/html
    [:XRD
     [:Link
      [:Property {:type "http://apinamespace.org/atom/username"}
       username]]])))

(defn mock-xrd-with-subject
  [subject]
  (cm/string->document
   (hiccup/html
    [:XRD
     [:Subject subject]])))

(test-environment-fixture

 (context #'get-username-from-atom-property
   (context "when the property has an identifier"
     (let [username (fseq :username)
           user-meta (mock-xrd-with-username username)]
       (get-username-from-atom-property user-meta) => username)))

 (future-context #'get-links
   (context "When it has links"
     (let [xrd nil]
       (get-links xrd)) => seq?))

 (context #'get-identifiers
   (let [subject "acct:foo@bar.baz"
         xrd (mock-xrd-with-subject subject)]
     (get-identifiers xrd) => (contains subject)))

 (context #'get-username-from-identifiers
   (let [subject "acct:foo@bar.baz"
         xrd (mock-xrd-with-subject subject)]
     (get-username-from-identifiers xrd) => "foo"))

 (context #'get-username-from-xrd
   (let [username (fseq :username)
         domain (fseq :domain)
         subject (format "acct:%s@%s" username domain)
         user-meta (mock-xrd-with-subject subject)]
     (context "when the usermeta has an identifier"
       (get-username-from-xrd user-meta) => username
       (provided
         (get-username-from-identifiers user-meta) => username
         (get-username-from-atom-property user-meta) => nil :times 0))
     (context "when the usermeta does not have an identifier"
       (context "and the atom link has an identifier"
         (let [user-meta (cm/string->document
                          (hiccup/html
                           [:XRD
                            [:Link {:ref ns/updates-from
                                    :type "application/atom+xml"
                                    :href ""}]]))]
           (get-username-from-xrd user-meta) => .username.
           (provided
             (get-username-from-identifiers user-meta) => nil
             (get-username-from-atom-property user-meta) => .username.)))
       (context "and the atom link does not have an identifier"
         (let [user-meta (cm/string->document
                          (hiccup/html
                           [:XRD
                            [:Link {:ref ns/updates-from
                                    :type "application/atom+xml"
                                    :href ""}]]))]
           (get-username-from-xrd user-meta) => nil
           (provided
             (get-username-from-identifiers user-meta) => nil
             (get-username-from-atom-property user-meta) => nil))))))

 )
