(ns jiksnu.modules.web.filters.client-filters-test
  (:use [ciste.core :only [with-serialization with-format
                           *serialization* *format*]]
        [ciste.filters :only [filter-action]]
        [jiksnu.test-helper :only [check context future-context test-environment-fixture]]
        [jiksnu.routes :only [app]]
        [midje.sweet :only [=>]])
  (:require [clj-factory.core :only [factory]]
            [clj-tigase.core :as tigase]
            [clj-tigase.element :as element]
            [clojure.tools.logging :as log]
            [jiksnu.namespace :as ns]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.mock :as mock]
            [jiksnu.model :as model]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.user :as model.user])
  )




