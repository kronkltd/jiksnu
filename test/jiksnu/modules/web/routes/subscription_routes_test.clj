(ns jiksnu.modules.web.routes.subscription-routes-test
  (:use [clj-factory.core :only [factory fseq]]
        [clojurewerkz.route-one.core :only [add-route! named-path]]
        [jiksnu.routes-helper :only [response-for]]
        [jiksnu.test-helper :only [check context future-context
                                   hiccup->doc test-environment-fixture]]
        [midje.sweet :only [=> truthy]])
  (:require [clojure.tools.logging :as log]
            [clojurewerkz.support.http.statuses :as status]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.mock :as mock]
            [jiksnu.features-helper :as feature]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.user :as model.user]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.actions.user-actions :as actions.user]
            [net.cgrand.enlive-html :as enlive]
            [ring.mock.request :as req]))

(test-environment-fixture

 (context "ostatus submit"
   (context "when not authenticated"
     (context "when it is a remote user"
       (let [username (fseq :username)
             domain-name (fseq :domain)]
         (-> (req/request :post "/main/ostatussub")
             (assoc :params {:profile
                             (format "acct:%s@%s" username domain-name)})
             response-for) =>
             (check [response]
               response => map?
               (:status response) => status/redirect?)))))

 (context "get-subscriptions"
   (let [user (mock/a-user-exists)
         path (named-path "user subscriptions" {:id (str (:_id user))})]
     (-> (req/request :get path)
         response-for)) =>
         (check [response]
           response => map?
           (:status response) => status/success?
           (:body response) => string?
           (let [doc (hiccup->doc (:body response))]
             (enlive/select doc [:.subscriptions]) => truthy)))
 )
