(ns jiksnu.model-test
  (:use [jiksnu.model :only [parse-http-link rel-filter]]
        [midje.sweet :only [fact future-fact => every-checker contains]]))

(fact "#'parse-http-link"
  (let [link-string "<http://jonkulp.dyndns-home.com/micro/main/xrd?uri=acct:jonkulp@jonkulp.dyndns-home.com>; rel=\"lrdd\"; type=\"application/xrd+xml\""]
    
    (parse-http-link link-string) =>
    (every-checker
     (contains {"href" "http://jonkulp.dyndns-home.com/micro/main/xrd?uri=acct:jonkulp@jonkulp.dyndns-home.com"})
     (contains {"rel" "lrdd"})
     (contains {"type" "application/xrd+xml"}))))

(fact "#'rel-filter"
  (let [links [{:rel "alternate"}
               {:rel "contains"}]]
    (fact "when the link exists"
      (rel-filter "alternate" links nil) => [{:rel "alternate"}])
    (fact "when the link does not exist"
      (rel-filter "foo" links nil) => [])))
