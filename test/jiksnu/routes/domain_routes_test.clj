(ns jiksnu.routes.domain-routes-test
  (:use [ciste.core :only [with-context]]
        [ciste.sections.default :only [uri]]
        [clj-factory.core :only [factory]]
        [jiksnu.routes-helper :only [response-for]]
        [jiksnu.test-helper :only [check context future-context test-environment-fixture]]
        [midje.sweet :only [=>]])
  (:require [clojure.tools.logging :as log]
            [clojurewerkz.support.http.statuses :as status]
            [hiccup.core :as h]
            [jiksnu.mock :as mock]
            [jiksnu.model :as model]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.domain :as model.domain]
            [ring.mock.request :as req]))

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

 (context "webfinger-host-meta"
   (context "should return a XRD document"
     (-> (req/request :get "/.well-known/host-meta")
         response-for) =>
         (check [response]
           response => map?
           (:status response) => status/success?
           (let [body (:body response)]
             body => #"<XRD.*"))))
 )
