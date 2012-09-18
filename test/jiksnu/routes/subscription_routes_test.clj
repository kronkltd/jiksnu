(ns jiksnu.routes.subscription-routes-test
  (:use [clj-factory.core :only [factory fseq]]
        [jiksnu.routes-helper :only [response-for]]
        [jiksnu.test-helper :only [hiccup->doc test-environment-fixture]]
        [midje.sweet :only [fact future-fact => every-checker truthy]])
  (:require [clojure.tools.logging :as log]
            [clojurewerkz.support.http.statuses :as status]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.features-helper :as feature]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.user :as model.user]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.actions.user-actions :as actions.user]
            [net.cgrand.enlive-html :as enlive]
            [ring.mock.request :as mock]))

(test-environment-fixture

 (fact "ostatus submit"
   (fact "when not authenticated"
     (fact "when it is a remote user"
       (let [username (fseq :username)
             domain-name (fseq :domain)]
         (-> (mock/request :post "/main/ostatussub")
             (assoc :params {:profile
                             (format "acct:%s@%s" username domain-name)})
             response-for) =>
             (every-checker
              map?
              #(status/redirect? (:status %)))))))

 (fact "get-subscriptions"
   (let [user (feature/a-user-exists)]
     (-> (mock/request :get (format "/%s/subscriptions" (:username user)))
         response-for)) =>
         (every-checker
          map?
          #(status/success? (:status %))
          (fn [response]
            (let [doc (hiccup->doc (:body response))]
              (-> doc
                  (enlive/select [:.subscriptions])) => truthy))))
 )
