(ns jiksnu.model.user-test
  (:use (ciste [config :only [config with-environment]]
               [debug :only [spy]])
        (clj-factory [core :only [factory fseq]])
        clojure.test
        (jiksnu test-helper model)
        jiksnu.model.user
        midje.sweet)
  (:require (clj-tigase [packet :as packet])
            (jiksnu.actions [domain-actions :as actions.domain]
                            [user-actions :as actions.user])
            (jiksnu.model [domain :as model.domain]))
  (:import jiksnu.model.Domain
           jiksnu.model.User))


(test-environment-fixture

  (fact "get-domain"
    (let [domain (actions.domain/create (factory Domain))
          user (actions.user/create (factory User {:domain (:_id domain)}))]
      (get-domain nil) => nil
      (get-domain user) => domain))

  (fact "local?"
    (fact "when there is a user"
      (fact "and it's domain is the same as the current domain"
        (fact "should be true"
          (let [domain (config :domain)
                user (factory User {:domain domain})]
            (local? user) => truthy)))
      (fact "and it's domain is different from the current domain"
        (fact "should be false"
          (let [domain (fseq :domain)
                user (factory User {:domain domain})]
            (local? user) => falsey)))))

  (fact "rel-filter"
    (let [links [{:rel "alternate"}
                 {:rel "contains"}]]
      (rel-filter "alternate" links) => [{:rel "alternate"}]
      (rel-filter "foo" links) => []))

  (fact "split-uri"
    (split-uri "bob@example.com") => ["bob" "example.com"]
    (split-uri "acct:bob@example.com") => ["bob" "example.com"]
    (split-uri "http://example.com/bob") => nil)

  (fact "display-name"
    (display-name .user.) => string?)

  (fact "get-link"
    (let [user (factory User {:links [{:rel "foo" :href "bar"}]})]
      (get-link user "foo") => (contains {:href "bar"})
      (get-link user "baz") => nil))

  ;; TODO: This is a better test for actions
  (fact "index"
    (fact "when there are no users"
      (fact "should be empty"
        ;; TODO: all collections should be emptied in background
        (drop!)
        (index) => empty?))

    (fact "when there are users"
      (fact "should not be empty"
        (actions.user/create (factory User))
        (index) => seq?)
      (fact "should return a seq of users"
        (actions.user/create (factory User))
        (index) => (partial every? user?))))

  (fact "#'get-user"
    (fact "when the user is found"
      (fact "should return a user"
        (let [username (fseq :id)
              domain (actions.domain/create (factory Domain))]
          (actions.user/create (factory User {:username username
                                              :domain (:_id domain)}))
          (get-user username (:_id domain)) => user?)))

    (fact "when the user is not found"
      (fact "should return nil"
        (drop!)
        (let [username (fseq :id)
              domain (actions.domain/create (factory Domain))]
          (get-user username (:_id domain)) => nil))))

  (fact "user-meta-uri"
    (fact "when the user's domain does not have a lrdd link"
      (fact "should return nil"
        (model.domain/drop!)
        (let [user (actions.user/create (factory User))]
          (user-meta-uri user) => nil)))

    (fact "when the user's domain has a lrdd link"
      (fact "should insert the user's uri into the template"
        (let [domain (actions.domain/create
                      (factory Domain
                               {:links [{:rel "lrdd"
                                         :template "http://example.com/main/xrd?uri={uri}"}]}))
              user (actions.user/create
                    (factory User {:domain (:_id domain)}))]
          (user-meta-uri user) => (str "http://example.com/main/xrd?uri=" (get-uri user))))))

  (fact "vcard-request"
    (let [user (actions.user/create (factory User))]
      (vcard-request user) => packet/packet?)))

