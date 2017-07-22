(ns jiksnu.modules.web.routes.domain-routes-test
  (:require [ciste.model :as cm]
            [clj-factory.core :refer [factory]]
            [clojure.data.json :as json]
            [jiksnu.modules.core.actions.domain-actions :as actions.domain]
            [jiksnu.helpers.routes :refer [response-for]]
            [jiksnu.test-helper :as th]
            [jiksnu.util :as util]
            [midje.sweet :refer :all]
            [ring.mock.request :as req])
  (:import (org.apache.http HttpStatus)))

(defn get-link
  [body rel]
  (let [pattern (format "//*[local-name() = 'Link'][@rel = '%s']" rel)]
    (cm/query body pattern)))

(th/module-test ["jiksnu.modules.core"
                 "jiksnu.modules.web"])

(facts "route: well-known/host-meta :get"
  (fact "host meta json"
    (let [domain (actions.domain/current-domain)
          url "/.well-known/host-meta"
          request (req/request :get url)
          response (response-for request)
          body (json/read-str (:body response) :key-fn keyword)]
      response =>
      (contains {:status HttpStatus/SC_OK
                 :body   string?
                 :headers
                 (contains {"Content-Type" "application/json;charset=UTF-8"})})
      body => (contains {:links #(>= (count %) 1)
                         :host (:_id domain)})
      (get-link body "lrdd") =not=> empty?
      (util/rel-filter "lrdd" (:links body)) =not=> empty?)))
