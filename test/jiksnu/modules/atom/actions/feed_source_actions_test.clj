(ns jiksnu.modules.atom.actions.feed-source-actions-test
  (:require [ciste.core :only [with-context]]
            [ciste.model :as cm]
            [ciste.sections.default :only [show-section]]
            [clj-factory.core :only [factory fseq]]
            [clojure.tools.logging :as log]
            [jiksnu.modules.atom.util :as abdera]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.actions.feed-source-actions :as actions.feed-source]
            [jiksnu.actions.resource-actions :as actions.resource]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.mock :as mock]
            [jiksnu.factory :as factory]
            [jiksnu.model :as model]
            [jiksnu.model.resource :as model.resource]
            [jiksnu.ops :as ops]
            [jiksnu.test-helper :only [check context future-context test-environment-fixture]]
            [jiksnu.util :as util]
            [lamina.core :as l]
            [midje.sweet :only [=> truthy anything]])
  (:import jiksnu.model.Activity
           jiksnu.model.FeedSource))

(test-environment-fixture

 (context #'process-entry
   (with-context [:http :atom]
     (let [user (mock/a-user-exists)
           author (show-section user)
           entry (show-section (factory :activity {:id (fseq :uri)}))
           feed (abdera/make-feed*
                 {:title (fseq :title)
                  :entries [entry]
                  :author author})
           source (mock/a-feed-source-exists)]
       (process-entry [feed source entry]) => (partial instance? Activity))))

 (context #'process-feed
   (context "when the feed has no watchers"
     (let [domain (mock/a-domain-exists)
           source (mock/a-feed-source-exists {:domain domain})
           feed (abdera/make-feed*
                 {:title (fseq :title)
                  :entries []})]
       (process-feed source feed) => nil
       (provided
         (unsubscribe source) => nil)))))

