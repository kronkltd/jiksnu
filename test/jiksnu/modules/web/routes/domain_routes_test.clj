(ns jiksnu.modules.web.routes.domain-routes-test
  (:require [ciste.model :as cm]
            [ciste.sections.default :refer [uri]]
            [clj-factory.core :refer [factory]]
            [clojure.data.json :as json]
            [clojurewerkz.support.http.statuses :as status]
            [hiccup.core :as h]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.mock :as mock]
            [jiksnu.model :as model]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.domain :as model.domain]
            [jiksnu.routes-helper :refer [response-for]]
            [jiksnu.test-helper :as th]
            [jiksnu.util :as util]
            [midje.sweet :refer :all]
            [ring.mock.request :as req]))

(defn get-link
  [body rel]
  (let [pattern (format "//*[local-name() = 'Link'][@rel = '%s']" rel)]
    (cm/query body pattern)))

(namespace-state-changes
 [(before :contents (th/setup-testing))
  (after :contents (th/stop-testing))])

(facts "Requesting the host meta"
  (fact "host meta json"
    (let [domain (actions.domain/current-domain)
          url "/.well-known/host-meta"
          request (req/request :get url)
          response (response-for request)
          body (json/read-str (:body response) :key-fn keyword)]
      response =>
      (contains {:status status/success?
                 :body string?
                 :headers
                 (contains {"Content-Type" "application/json;charset=UTF-8"})})
      body => (contains {:links #(>= (count %) 1)
                         :host (:_id domain)})
      (get-link body "lrdd") =not=> empty?
      (util/rel-filter "lrdd" (:links body)) =not=> empty?)))
