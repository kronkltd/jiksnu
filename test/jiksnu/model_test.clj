(ns jiksnu.model-test
  (:use [midje.sweet :only [fact future-fact => every-checker contains]])
  (:require [jiksnu.util :as util])
  (:import org.bson.types.ObjectId))

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

(fact "make-id"
  (make-id) => (partial instance? ObjectId))

(fact "#'path-segments"
  (path-segments "http://example.com/status/users/1") =>
  '("http://example.com/"
    "http://example.com/status/"
    "http://example.com/status/users/"))

(fact "#'rel-filter"
  (let [links [{:rel "alternate"}
               {:rel "contains"}]]
    (fact "when the link exists"
      (rel-filter "alternate" links nil) => [{:rel "alternate"}])
    (fact "when the link does not exist"
      (rel-filter "foo" links nil) => [])))
