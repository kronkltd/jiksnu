(ns jiksnu.views.activity-views-test
  (:use ciste.core
        ciste.sections
        ciste.views
        clj-factory.core
        clj-tigase.core
        clojure.test
        jiksnu.core-test
        jiksnu.actions.activity-actions
        jiksnu.model
        jiksnu.session
        jiksnu.view
        jiksnu.views.activity-views)
  (:require [jiksnu.model.activity :as model.activity]
            [jiksnu.model.user :as model.user])
  (:import org.apache.abdera2.model.Entry
           jiksnu.model.Activity
           jiksnu.model.User))

(use-fixtures :once test-environment-fixture)

