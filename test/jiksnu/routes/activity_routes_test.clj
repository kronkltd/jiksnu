(ns jiksnu.routes.activity-routes-test
  (:use [ciste.core :only [with-context]]
        [ciste.sections.default :only [full-uri]]
        [clj-factory.core :only [factory fseq]]
        [jiksnu.routes-helper :only [as-user response-for]]
        [jiksnu.test-helper :only [test-environment-fixture]]
        [midje.sweet :only [contains every-checker fact future-fact =>]])
  (:require [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            [clojurewerkz.support.http.statuses :as status]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.user :as model.user]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.actions.auth-actions :as actions.auth]
            [jiksnu.actions.user-actions :as actions.user]
            [ring.mock.request :as mock])
  (:import jiksnu.model.Activity
           jiksnu.model.User))


(test-environment-fixture

 ;; (fact "update"
 ;;   (fact "when the user is authenticated"
 ;;     (let [author (model.user/create (factory :local-user))
 ;;           content (fseq :content)
 ;;           data (json/json-str
 ;;                 {:content content})]
 ;;       data => string?)))
 
 (fact "show-http-route"
   ;; (future-fact "when the user is not authenticated"
   ;;   (fact "and the activity does not exist"
   ;;     (let [author (model.user/create (factory :local-user))
   ;;           activity (factory :activity)]
   ;;       (->> (str "/notice/" (:_id activity))
   ;;            (mock/request :get)
   ;;            response-for) =>
   ;;            (contains {:status 404})))

   ;;   (fact "and there are activities"
   ;;     (let [author (model.user/create (factory :local-user))
   ;;           activity (model.activity/create (factory :activity {:author (:_id author)}))]
   ;;       (->> (str "/notice/" (:_id activity))
   ;;            (mock/request :get)
   ;;            response-for) =>
   ;;            (every-checker
   ;;             (comp status/success? :status)
   ;;             (fn [response]
   ;;               (fact
   ;;                 (:body response) => (re-pattern (str (:_id activity)))))))))
   (fact "when the user is authenticated"
     (fact "when a private activity exists"
       (let [author (model.user/create (factory :local-user))
             activity (->> {:author (:_id author)
                            :public false}
                           (factory :activity)
                           model.activity/create)]
         (-> (mock/request :get (str "/notice/" (:_id activity)))
             as-user response-for) =>
             (every-checker
              map?
              (comp status/redirect? :status))))))
 
 ;; (fact "oembed"
 ;;   (fact "when the format is json"
 ;;     (let [activity (model.activity/create (factory :activity {:local true}))]
 ;;       (-> (mock/request :get (with-context [:http :html]
 ;;                                (str "/main/oembed?format=json&url=" (full-uri activity))))
 ;;           response-for) =>
 ;;           (every-checker
 ;;            map?
 ;;            (fn [response]
 ;;              (fact
 ;;                (:status response) => status/success?)))))
 ;;   (fact "when the format is xml"
 ;;     (let [activity (model.activity/create (factory :activity
 ;;                                                    {:local true}))]
 ;;       (-> (mock/request :get (with-context [:http :html]
 ;;                                (str "/main/oembed?format=xml&url=" (full-uri activity))))
 ;;           response-for) =>
 ;;           (every-checker
 ;;            map?
 ;;            (comp status/success? :status)))))
 )
