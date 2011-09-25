(ns jiksnu.filters.user-filters-test
  (:use clj-factory.core
        clj-tigase.core
        (ciste [config :only (config)]
               [debug :only (spy)])
        clojure.test
        (jiksnu core-test
                model
                [routes :only (app)]
                session
                view)
        lamina.core)
  (:require (jiksnu.actions [user-actions :as actions.user])
            (jiksnu.model [activity :as model.activity]
                          [user :as model.user])
            (ring.mock [request :as mock]))
  (:import (jiksnu.model Activity User)))

(use-fixtures :once test-environment-fixture)

