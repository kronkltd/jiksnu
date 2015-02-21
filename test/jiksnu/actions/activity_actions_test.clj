(ns jiksnu.actions.activity-actions-test
  (:require [ciste.sections.default :refer [show-section]]
            [clj-factory.core :refer [factory fseq]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.db :as db]
            [jiksnu.mock :as mock]
            [jiksnu.model :as model]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.session :as session]
            [jiksnu.test-helper :as th :refer [check]]
            [jiksnu.util :as util]
            [midje.sweet :refer :all])
  (:import jiksnu.model.Activity))

(namespace-state-changes
 [(before :contents (th/setup-testing))
  (after :contents (th/stop-testing))])

(fact "#'actions.activity/create"
  (fact "when the user is logged in"
    (fact "and it is a valid activity"
      (let [domain (mock/a-domain-exists)
            feed-source (mock/a-feed-source-exists)
            conversation (mock/a-conversation-exists)
            user (mock/a-user-exists)
            activity (factory :activity {:author        (:_id user)
                                         :conversation  (:_id conversation)
                                         :update-source (:_id feed-source)
                                         :local         true})]
        (actions.activity/create activity) => (partial instance? Activity)))))

(fact "#'actions.activity/delete"
  (fact "when the activity exists"
    (fact "and the user owns the activity"
      (let [user (mock/a-user-exists)]
        (session/with-user user
          (let [activity (mock/there-is-an-activity {:user user})]
            (actions.activity/delete activity) => activity
            (model.activity/fetch-by-id (:_id activity)) => nil))))
    (fact "and the user does not own the activity"
      (let [user (mock/a-user-exists)
            author (mock/a-remote-user-exists)
            activity (mock/there-is-an-activity {:user author})]
        (session/with-user user
          (actions.activity/delete activity) => (throws RuntimeException)
          (model.activity/fetch-by-id (:_id activity)) => activity)))))

(fact "#'actions.activity/find-by-user"
  (fact "when the user has activities"
    (db/drop-all!)
    (let [user (mock/a-user-exists)
          activity (mock/there-is-an-activity {:user user})]
      (actions.activity/find-by-user user) =>
      (contains {:totalItems 1
                 :items (has every? #(instance? Activity %))}))))

(facts "#'actions.activity/index"
  (fact "when there are no activities"
    (db/drop-all!)
    (actions.activity/index {}) => (contains {:totalItems 0})))

(fact "#'actions.activity/oembed->activity"
  (let [oembed-str (slurp "test-resources/oembed.json")]
    ;; TODO: complete
    oembed-str => string?))

(fact "#'actions.activity/post"
  (fact "when the user is not logged in"
    (let [activity (dissoc (factory :activity) :author)]
      (actions.activity/post activity) => (throws RuntimeException))))

(fact "#'actions.activity/show"
  (fact "when the record exists"
    (fact "and the record is viewable"
      (let [activity (mock/there-is-an-activity)]
        (actions.activity/show activity) => activity
        (provided
          (actions.activity/viewable? activity) => true)))
    (fact "and the record is not viewable"
      (let [activity (mock/there-is-an-activity)]
        (actions.activity/show activity) => (throws RuntimeException)
        (provided
          (actions.activity/viewable? activity) => false)))))

(fact "#'actions.activity/viewable?"
  (fact "When it is public"
    (let [activity (mock/there-is-an-activity)]
      (actions.activity/viewable? activity .user.)) => truthy)
  (fact "when it is not public"
    (fact "when the user is the author"
      (let [user (mock/a-user-exists)
            activity (mock/there-is-an-activity {:user user})]
        (actions.activity/viewable? activity user)) => truthy)
    (fact "when the user is not the author"
      (fact "when the user is an admin"
        (let [user (mock/a-user-exists {:admin true})
              activity (mock/there-is-an-activity {:modifier "private"})]
          (actions.activity/viewable? activity user)) => truthy)
      (fact "when the user is not an admin"
        (let [user (mock/a-user-exists)
              author (mock/a-user-exists)
              activity (mock/there-is-an-activity {:modifier "private"
                                                   :user author})]
          (actions.activity/viewable? activity user)) => falsey))))

(fact "#'actions.activity/edit"

  (fact "when the params are nil"
    (let [params nil]
      (actions.activity/edit params) => (throws)))

  (fact "when there is not an id"
    (let [params {}]
      (actions.activity/edit params) => (throws)))

  (fact "when the target is not found"
    (let [params {:_id (util/make-id)}]
      (actions.activity/edit params) => (throws)))

  (fact "when there is no actor"
    (let [activity (mock/there-is-an-activity)
          params {:_id (:_id activity)}]
      (actions.activity/edit params) => (throws)))

  (fact "when the activity is not editable"
    (let [activity (mock/there-is-an-activity)
          actor (mock/a-user-exists)
          params {:_id activity}]
      (session/with-user actor
        (actions.activity/edit params) => (throws))))

  (fact "when the provided params contain invalid keys"
    (let [actor (mock/a-user-exists)
          activity (mock/there-is-an-activity {:user actor})
          params {:_id (:_id activity)
                  :author "acct:foo@bar.baz"}]
      (session/with-user actor
        (actions.activity/edit params) => (throws))))

  (fact "when the params are valid"
    (let [actor (mock/a-user-exists)
          activity (mock/there-is-an-activity {:user actor})
          params {:_id (:_id activity)
                  :title (fseq :title)
                  :nsfw true}]
      (session/with-user actor
        (actions.activity/edit params) => (partial instance? Activity)
        (model.activity/fetch-by-id (:_id activity)) =>
        (contains {:title (:title params)
                   :nsfw true})))))

(fact "#'actions.activity/oembed"
  (let [activity (mock/there-is-an-activity)]
    (actions.activity/oembed activity) =>
    (contains {:html string?})))

(fact "#'actions.activity/fetch-by-conversation"
  (fact "when there are matching activities"
    (let [conversation (mock/a-conversation-exists)
          activity (mock/there-is-an-activity {:conversation conversation})]
      (actions.activity/fetch-by-conversation conversation) =>
      (contains {:items #(= (count %) 1)}))))

(fact "#'actions.activity/fetch-by-conversations"
  (fact "when there are matching activities"
    (let [conversation1 (mock/a-conversation-exists)
          conversation2 (mock/a-conversation-exists)
          activity1 (mock/there-is-an-activity {:conversation conversation1})
          activity2 (mock/there-is-an-activity {:conversation conversation2})
          ids [(:_id conversation1) (:_id conversation2)]]
      (actions.activity/fetch-by-conversations ids) =>
      (contains {:items #(= (count %) 1)}))))
