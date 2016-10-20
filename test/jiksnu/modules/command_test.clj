(ns jiksnu.modules.command-test
  (:require [ciste.commands :refer [parse-command]]
            [ciste.core :refer [with-context]]
            [clj-factory.core :refer [factory]]
            [clojure.data.json :as json]
            [jiksnu.mock :as mock]
            [jiksnu.modules.command]
            [jiksnu.test-helper :as th]
            [manifold.deferred :as d]
            [midje.sweet :refer :all]))

(th/module-test ["jiksnu.modules.core"
                 "jiksnu.modules.json"
                 "jiksnu.modules.command"
                 ;; FIXME: This shouldn't be required
                 "jiksnu.modules.web"])

(facts "command 'get-model user'"
  (let [command "get-model"
        ch (d/deferred)
        type "user"]

    (future-fact "when the record is not found"
      (let [request {:format :json
                     :channel ch
                     :name command
                     :args [type "acct:foo@bar.baz"]}]
        (let [response (parse-command request)]
          (let [m (json/read-str response)]
            (get m "action") => "error"))))

    (fact "when the record is found"
      (let [user (mock/a-user-exists)
            request {:channel ch
                     :name command
                     :format :json
                     :args (list "user" (:_id user))}]
        (some-> request parse-command (json/read-str :key-fn keyword)) =>
        (contains {:action "model-updated"})))))

(fact "command 'get-page activities'"
  (let [name "get-page"
        args '("activities")]
    (fact "when there are activities"
      (let [activity (mock/an-activity-exists)
            ch (d/deferred)
            request {:channel ch
                     :name name
                     :format :json
                     :args args}]
        (some-> request parse-command (json/read-str :key-fn keyword)) =>
        (contains {:items (contains (str (:_id activity)))})))))

(fact "command 'get-page clients'"
  (let [name "get-page"
        args '("clients")]
    (fact "when there are clients"
      (let [client (mock/a-client-exists)
            ch (d/deferred)
            request {:channel ch
                       :name name
                       :format :json
                       :args args}
              response (parse-command request)]
        (some-> request parse-command (json/read-str :key-fn keyword)) =>
        map?))))

(fact "command 'get-page streams'"
  (let [name "get-page"
        args '("streams")
        ch (d/deferred)
        request {:name name
                 :channel ch
                 :format :json
                 :args args}
        response (parse-command request)]
    (some-> request parse-command (json/read-str :key-fn keyword)) =>
    map?))

(fact "command 'get-sub-page Activity likes"
  (fact " - when activity exists"
    (fact " - - and there are no likes"
      (let [ch (d/deferred)
            command "get-sub-page"
            user (mock/a-user-exists)
            activity (mock/an-activity-exists {:user user})
            model-name "activity"
            id (str (:_id activity))
            page-name "likes"
            request {:channel ch
                     :format :json
                     :name command
                     :args (list model-name id page-name)}]
        (some-> request parse-command (json/read-str :key-fn keyword)) =>
        (contains {:totalItems 0})))))

(fact "command 'get-sub-page Users activitites"
  (let [ch (d/deferred)
        command "get-sub-page"
        user (mock/a-user-exists)
        activity (mock/an-activity-exists :user user)
        model-name "user"
        id (:_id user)
        page-name "activities"
        request {:channel ch
                 :format :json
                 :name command
                 :args (list model-name id page-name)}
        response (parse-command request)]
    (some-> request parse-command (json/read-str :key-fn keyword)) =>
    (contains {:totalItems 1})))
