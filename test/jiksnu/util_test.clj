(ns jiksnu.util-test
  (:use [clj-factory.core :only [fseq]]
        [jiksnu.test-helper :only [test-environment-fixture]]
        jiksnu.util
        [midje.sweet :only [fact future-fact => every-checker contains]])
  (:require [jiksnu.util :as util])
  (:import org.bson.types.ObjectId))

(test-environment-fixture

 (fact "#'get-domain-name"
   (let [domain-name (fseq :domain)]
     (fact "when given a http uri"
       (let [uri (str "http://" domain-name "/users/1")]
         (get-domain-name uri) => domain-name))

     (fact "when given an acct uri"
       (let [uri (str "acct:bob@" domain-name)]
         (get-domain-name uri) => domain-name))

     (fact "when given a urn"
       (let [uri (str "urn:X-dfrn:"
                      domain-name
                      ":1:4735de37f18b820836fbe17890b33f90781d4fe275236094751be3fc163b40b4")]
         (get-domain-name uri) => domain-name))))

 (fact "make-id"
   (make-id) => (partial instance? ObjectId))

 (fact "#'path-segments"
   (path-segments "http://example.com/status/users/1") =>
   '("http://example.com/"
     "http://example.com/status/"
     "http://example.com/status/users/"))

 (future-fact "#'rel-filter"
   (let [links [{:rel "alternate"}
                {:rel "contains"}]]
     (fact "when the link exists"
       (util/rel-filter "alternate" links nil) => [{:rel "alternate"}])
     (fact "when the link does not exist"
       (util/rel-filter "foo" links nil) => [])))

 (fact "#'parse-http-link"
   (let [uri "acct:jonkulp@jonkulp.dyndns-home.com"
         url (str "http://jonkulp.dyndns-home.com/micro/main/xrd?uri=" uri)
         rel "lrdd"
         content-type "application/xrd+xml"
         link-string (format "<%s>; rel=\"%s\"; type=\"%s\""
                             url rel content-type)]
     (parse-http-link link-string) =>
     (every-checker
      (contains {"href" url})
      (contains {"rel" "lrdd"})
      (contains {"type" "application/xrd+xml"}))))

 (fact "split-uri"
   (split-uri "bob@example.com")        => ["bob" "example.com"]
   (split-uri "acct:bob@example.com")   => ["bob" "example.com"]
   (split-uri "http://example.com/bob") => nil)

 )
