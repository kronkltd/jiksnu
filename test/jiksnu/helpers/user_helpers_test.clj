(ns jiksnu.helpers.user-helpers-test
  (:use [ciste.config :only [with-environment]]
        clj-factory.core
        midje.sweet
        jiksnu.test-helper
        jiksnu.model
        jiksnu.helpers.user-helpers)
  (:require [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.model.domain :as model.domain]
            [jiksnu.model.user :as model.user]
            [jiksnu.model.webfinger :as model.webfinger])
  (:import jiksnu.model.Domain
           jiksnu.model.User))

(test-environment-fixture

)

