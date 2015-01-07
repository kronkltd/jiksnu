(ns jiksnu.util-test
  (:require [clj-factory.core :refer [fseq]]
            [jiksnu.test-helper :as th]
            [jiksnu.util :as util]
            [midje.sweet :refer :all])
  (:import org.bson.types.ObjectId))

(namespace-state-changes
 [(before :contents (th/setup-testing))
  (after :contents (th/stop-testing))])

(fact #'util/new-id
  (util/new-id) => string?)

(fact #'util/get-domain-name
  (let [domain-name (fseq :domain)]
    (fact "when given a http uri"
      (let [uri (str "http://" domain-name "/users/1")]
        (util/get-domain-name uri) => domain-name))

    (fact "when given an acct uri"
      (let [uri (str "acct:bob@" domain-name)]
        (util/get-domain-name uri) => domain-name))

    (fact "when given a urn"
      (let [uri (str "urn:X-dfrn:"
                     domain-name
                     ":1:4735de37f18b820836fbe17890b33f90781d4fe275236094751be3fc163b40b4")]
        (util/get-domain-name uri) => domain-name))))

(fact #'util/make-id
  (util/make-id) => (partial instance? ObjectId))

(fact #'util/path-segments
  (fact "When the path ends without a slash"
    (let [url "http://example.com/status/users/1"]
      (util/path-segments url) =>
      '("/" "/status/" "/status/users/")))
  )

(future-fact #'util/rel-filter
  (let [links [{:rel "alternate"}
               {:rel "contains"}]]
    (fact "when the link exists"
      (util/rel-filter "alternate" links nil) => [{:rel "alternate"}])
    (fact "when the link does not exist"
      (util/rel-filter "foo" links nil) => [])))

(fact #'util/parse-http-link
  (let [uri "acct:jonkulp@jonkulp.dyndns-home.com"
        url (str "http://jonkulp.dyndns-home.com/micro/main/xrd?uri=" uri)
        rel "lrdd"
        content-type "application/xrd+xml"
        link-string (format "<%s>; rel=\"%s\"; type=\"%s\""
                            url rel content-type)
        link {"href" url
              "rel" rel
              "type" content-type}]
    (util/parse-http-link link-string) => (contains link)))

(fact #'util/split-uri
  (util/split-uri "bob@example.com")        => ["bob" "example.com"]
  (util/split-uri "acct:bob@example.com")   => ["bob" "example.com"]
  (util/split-uri "http://example.com/bob") => nil)


