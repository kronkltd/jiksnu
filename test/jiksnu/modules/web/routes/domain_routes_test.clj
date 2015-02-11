(ns jiksnu.modules.web.routes.domain-routes-test
  (:require [ciste.core :refer [with-context]]
            [ciste.model :as cm]
            [ciste.sections.default :refer [uri]]
            [clj-factory.core :refer [factory]]
            [clojure.data.json :as json]
            [clojure.tools.logging :as log]
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
 [(before :contents (setup-testing))
  (after :contents (stop-testing))])

(future-facts "Requesting the host meta"
  (against-background
    [(actions.domain/current-domain) => .domain.
     (actions.domain/show .domain.) => .domain.]

    (fact "returns the host meta as xml"
      (let [url "/.well-known/host-meta"]
        (response-for (req/request :get url)) =>
        (contains {:status status/success?
                   :headers (contains {"Content-Type" "application/xrds+xml"})
                   :body #(seq (get-link (cm/string->document %) "lrdd"))})))

    (fact "host meta json"
      (let [url "/.well-known/host-meta.json"
            response (response-for (req/request :get url))]
        response => map?
        (:status response) => status/success?
        (:body response) => string?
        (get-in response [:headers "Content-Type"]) => "application/json"
        (let [body (json/read-str (:body response) :key-fn keyword)]
          (count (:links body)) => (partial >= 1)
          (:host body) => (:_id .domain.)
          (get-link body "lrdd") =not=> empty?
          (util/rel-filter "lrdd" (:links body)) =not=> empty?)))
    ))


