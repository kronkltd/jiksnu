(ns jiksnu.routes.domain-routes-test
  (:use [ciste.core :only [with-context]]
        [ciste.model :as cm]
        [ciste.sections.default :only [uri]]
        [clj-factory.core :only [factory]]
        [jiksnu.routes-helper :only [response-for]]
        [jiksnu.test-helper :only [check context future-context test-environment-fixture]]
        [midje.sweet :only [=>]])
  (:require [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            [clojurewerkz.support.http.statuses :as status]
            [hiccup.core :as h]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.mock :as mock]
            [jiksnu.model :as model]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.domain :as model.domain]
            [jiksnu.util :as util]
            [ring.mock.request :as req]))

(defn get-link
  [body rel]
  (let [pattern (format "//*[local-name() = 'Link'][@rel = '%s']" rel)]
    (cm/query body pattern)))

(test-environment-fixture

 (context "show"
   (with-context [:http :html]
     (let [domain (mock/a-domain-exists)
           user (mock/a-user-exists {:domain domain})]

       (context "when requesting the default page"
         (-> (req/request :get (uri domain))
             response-for) =>
             (check [response]
               response => map?
               (:status response) => status/success?
               (let [body (h/html (:body response))]
                 body => (re-pattern (str (:_id domain))))))

       (context "when requesting the second page of users"
         (let [path (str (uri domain)
                         "?page=2")]
           (-> (req/request :get path)
               response-for)) =>
               (check [response]
                 response => map?
                 (:status response) => status/success?
                 (let [body (h/html (:body response))]
                   body => (re-pattern (str (:_id domain))))))
       )))

 (context "Requesting the host meta"
   (let [domain (actions.domain/current-domain)]

     (context "returns the host meta as xml"
       (->> "/.well-known/host-meta"
            (req/request :get)
            response-for) =>
            (check [response]
              response => map?
              (:status response) => status/success?
              (:body response) => string?
              (get-in response [:headers "Content-Type"]) => "application/xrds+xml"
              (let [body (cm/string->document (:body response))]
                ;; has at least 1 lrdd link
                (get-link body "lrdd") =not=> empty?)))

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
                (:host body) => (:_id domain)

                ;; has at least 1 lrdd link
                (get-link body "lrdd") =not=> empty?

                ;; has a lrdd link
                (util/rel-filter "lrdd" (:links body)) =not=> empty?)))
     ))

 )
