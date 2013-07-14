(ns jiksnu.routes.webfinger-routes-test
  (:use [ciste.config :only [config]]
        [jiksnu.routes-helper :only [response-for]]
        [jiksnu.test-helper :only [check context future-context test-environment-fixture]]
        [midje.sweet :only [=> =not=>]])
  (:require [ciste.model :as cm]
            [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            [clojurewerkz.support.http.statuses :as status]
            [jiksnu.util :as util]
            [ring.mock.request :as req]))

(test-environment-fixture

 (defn get-link
   [body rel]
   (let [pattern (format "//*[local-name() = 'Link'][@rel = '%s']" rel)]
     (cm/query pattern body)))

 (context "Requesting the host meta"
   (context "returns the host meta as xml"
     (->> "/.well-known/host-meta"
          (req/request :get)
          response-for) =>
          (check [response]
            response => map?
            (:status response) => status/redirect?
            (:body response) => string?
            (get-in response [:headers "Content-Type"]) => "application/xrds+xml"
            (let [body (cm/string->document (:body response))]
              ;; has at least 1 lrdd link
              (get-link body "lrdd") =not=> empty?))))

 (context "host meta json"
   (->> "/.well-known/host-meta.json"
        (req/request :get)
        response-for) =>
        (check [response]
          response => map?
          (:status response) => status/success?
          (:body response) => string?
          (get-in response [:headers "Content-Type"]) => "application/json"
          (let [body (json/read-json (:body response))]
            ;; has at least 1 link
            (count (:links body)) => (partial >= 1)

            ;; host property matches domain
            ;; NB: this will probably be removed
            (:host body) => (config :domain)

            ;; has at least 1 lrdd link
            (get-link body "lrdd") =not=> empty?

            ;; has a lrdd link
            (util/rel-filter "lrdd" (:links body)) =not=> empty?)))

 )
