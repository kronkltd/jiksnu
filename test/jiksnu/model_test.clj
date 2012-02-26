(ns jiksnu.model-test
  (:use midje.sweet
        jiksnu.model
        )
  )

(fact "#'parse-http-link"
  (let [link-string "<http://jonkulp.dyndns-home.com/micro/main/xrd?PHPSESSID=17v5lqgsbdpggp3am4u9brfcd0?uri=acct:jonkulp@jonkulp.dyndns-home.com>; rel=\"lrdd\"; type=\"application/xrd+xml\""]
    (let [response (parse-http-link link-string)]
      response => (contains {"href" "http://jonkulp.dyndns-home.com/micro/main/xrd?PHPSESSID=17v5lqgsbdpggp3am4u9brfcd0?uri=acct:jonkulp@jonkulp.dyndns-home.com"})
      response => (contains {"rel" "lrdd"})
      response => (contains {"type" "application/xrd+xml"})
      )
    )
  )
