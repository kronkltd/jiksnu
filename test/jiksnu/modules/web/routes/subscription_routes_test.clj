(ns jiksnu.modules.web.routes.subscription-routes-test
  (:require [clj-factory.core :refer [factory fseq]]
            [clojure.tools.logging :as log]
            [clojurewerkz.support.http.statuses :as status]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.mock :as mock]
            [jiksnu.model :as model]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.user :as model.user]
            [jiksnu.ops :as ops]
            [jiksnu.routes.helpers :refer [add-route! named-path]]
            [jiksnu.routes-helper :refer [as-user response-for]]
            [jiksnu.test-helper :refer [check context future-context
                                        hiccup->doc test-environment-fixture]]
            [lamina.core :as l]
            [midje.sweet :refer [=> anything truthy]]
            [net.cgrand.enlive-html :as enlive]
            [ring.mock.request :as req]))

(test-environment-fixture

 (context "ostatus submit"
   (let [username (fseq :username)
         domain-name (fseq :domain)
         uri (format "acct:%s@%s" username domain-name)
         params {:profile uri}]

     (context "when not authenticated"
       (context "when it is a remote user"
         (-> (req/request :post "/main/ostatussub")
             (assoc :params params)
             response-for) =>
             (check [response]
               response => map?
               (:status response) => status/redirect?)))

     (context "when authenticated"
       (let [actor (mock/a-user-exists)]
         (context "when it is a remote user"
           (-> (req/request :post "/main/ostatussub")
               (assoc :params params)
               (as-user actor)
               response-for) =>
               (check [response]
                 response => map?
                 (:status response) => status/redirect?)
               (provided
                 (ops/get-discovered anything) => (l/success-result
                                                   (model/map->Domain {:_id domain-name}))))))
     ))

 (context "get-subscriptions"
   (let [user (mock/a-user-exists)
         subscription (mock/a-subscription-exists {:from user})
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
