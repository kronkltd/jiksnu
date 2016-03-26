(ns jiksnu.actions.group-membership-actions-test
  (:require [ciste.config :refer [config]]
            [ciste.sections.default :refer [show-section]]
            [clj-factory.core :refer [factory fseq]]
            [clojure.data.json :as json]
            [hiccup.core :as h]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.actions.group-membership-actions :as actions.group-membership]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.db :as db]
            [jiksnu.mock :as mock]
            [jiksnu.factory :as factory]
            [jiksnu.model :as model]
            [jiksnu.model.authentication-mechanism :as model.auth-mechanism]
            [jiksnu.model.domain :as model.domain]
            [jiksnu.namespace :as ns]
            [jiksnu.test-helper :as th]
            [jiksnu.util :as util]
            [midje.sweet :refer :all])
  (:import jiksnu.model.Domain
           jiksnu.model.User))

(th/module-test ["jiksnu.modules.core"])

(fact "fetch-by-group"
  (let [user (mock/a-user-exists)
        group (mock/a-group-exists)]

    (actions.group-membership/create
     {:user (:_id user)
      :group (:_id group)})

    (actions.group-membership/fetch-by-group group) =>
    (contains {:totalItems 1})))
