(ns jiksnu.actions.activity-actions-test
  (:require [ciste.core :refer [with-context]]
            [ciste.sections.default :refer [show-section]]
            [clj-factory.core :refer [factory fseq]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.db :as db]
            [jiksnu.mock :as mock]
            [jiksnu.model :as model]
            [jiksnu.model.activity :as model.activity]
            ;; [jiksnu.modules.atom.util :as abdera]
            [jiksnu.session :as session]
            [jiksnu.test-helper :refer [check context future-context test-environment-fixture]]
            [jiksnu.util :as util]
            [midje.sweet :refer [=> contains throws truthy falsey]])
  (:import jiksnu.model.Activity))

(test-environment-fixture

  (context #'actions.activity/entry->activity
   (let [domain-name (fseq :domain)
         domain (-> (factory :domain
                             {:discovered true
                              :_id domain-name})
                    actions.domain/find-or-create
                    (actions.domain/add-link {:rel "lrdd"
                                              :template
                                              (str "http://" domain-name "/lrdd?uri={uri}")}))
         user (mock/a-user-exists {:domain domain})]

     ;; TODO: Load elements from resources
     (context "should return an Activity"
       (with-context [:http :atom]
         (let [activity (mock/there-is-an-activity {:user user})
               entry (show-section activity)]
           (actions.activity/entry->activity entry) => (partial instance? Activity))))

     ;; (future-context "when coming from an identi.ca feed"
     ;;   (context "should parse the published field"
     ;;     (let [feed (abdera/load-file "identica-update.xml")
     ;;           entry (first (abdera/get-entries feed))]
     ;;       (actions.activity/entry->activity entry) => (partial instance? Activity))))
     ))


 )
