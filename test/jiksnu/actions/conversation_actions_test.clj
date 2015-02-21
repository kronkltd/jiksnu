(ns jiksnu.actions.conversation-actions-test
  (:require [clj-factory.core :refer [factory]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.conversation-actions :refer [create delete index show]]
            [jiksnu.factory :refer [make-uri]]
            [jiksnu.mock :as mock]
            [jiksnu.model.conversation :as model.conversation]
            [jiksnu.test-helper :as th]
            [midje.sweet :refer :all]))

(namespace-state-changes
 [(before :contents (th/setup-testing))
  (after :contents (th/stop-testing))])



(fact #'create
  (let [domain (mock/a-domain-exists)
        domain-name (:_id domain)
        url (make-uri domain-name)
        source (mock/a-feed-source-exists {:domain domain
                                           :url url})
        params (factory :conversation {:domain domain-name
                                       :url url
                                       :local (:local domain)
                                       :update-source (:_id source)})]
    (create params)) => map?)

(fact #'delete
  (let [conversation (mock/a-conversation-exists)]
    (delete conversation) => map?
    (model.conversation/fetch-by-id (:_id conversation)) => nil))

(fact #'index
  (fact "should return a page structure"
    (index) => map?))

(fact #'show
  (show .conversation.) => .conversation.)


