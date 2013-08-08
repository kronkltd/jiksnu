(ns jiksnu.util-test
  (:use [clj-factory.core :only [fseq]]
        [jiksnu.test-helper :only [check context future-context test-environment-fixture]]
        jiksnu.util
        [midje.sweet :only [=> contains]])
  (:require [jiksnu.util :as util])
  (:import org.bson.types.ObjectId))

(test-environment-fixture

 (context #'new-id
   (new-id) => string?)

 (context #'get-domain-name
   (let [domain-name (fseq :domain)]
     (context "when given a http uri"
       (let [uri (str "http://" domain-name "/users/1")]
         (get-domain-name uri) => domain-name))

     (context "when given an acct uri"
       (let [uri (str "acct:bob@" domain-name)]
         (get-domain-name uri) => domain-name))

     (context "when given a urn"
       (let [uri (str "urn:X-dfrn:"
                      domain-name
                      ":1:4735de37f18b820836fbe17890b33f90781d4fe275236094751be3fc163b40b4")]
         (get-domain-name uri) => domain-name))))

 (context #'make-id
   (make-id) => (partial instance? ObjectId))

 (context #'path-segments
   (path-segments "http://example.com/status/users/1") =>
   '("http://example.com/"
     "http://example.com/status/"
     "http://example.com/status/users/"))

 (future-context #'rel-filter
   (let [links [{:rel "alternate"}
                {:rel "contains"}]]
     (context "when the link exists"
       (util/rel-filter "alternate" links nil) => [{:rel "alternate"}])
     (context "when the link does not exist"
       (util/rel-filter "foo" links nil) => [])))

 (context #'parse-http-link
   (let [uri "acct:jonkulp@jonkulp.dyndns-home.com"
         url (str "http://jonkulp.dyndns-home.com/micro/main/xrd?uri=" uri)
         rel "lrdd"
         content-type "application/xrd+xml"
         link-string (format "<%s>; rel=\"%s\"; type=\"%s\""
                             url rel content-type)
         link {"href" url
               "rel" rel
               "type" content-type}]
     (parse-http-link link-string) => (contains link)))

 (context #'split-uri
   (split-uri "bob@example.com")        => ["bob" "example.com"]
   (split-uri "acct:bob@example.com")   => ["bob" "example.com"]
   (split-uri "http://example.com/bob") => nil)

 )
