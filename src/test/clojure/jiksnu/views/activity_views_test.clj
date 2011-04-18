(ns jiksnu.views.activity-views-test
  (:use ciste.core
        ciste.factory
        ciste.sections
        ciste.views
        clj-tigase.core
        jiksnu.config
        jiksnu.actions.activity-actions
        jiksnu.model
        jiksnu.session
        jiksnu.view
        jiksnu.views.activity-views
        [lazytest.describe :only (describe testing do-it)]
        [lazytest.expect :only (expect)])
  (:require [jiksnu.model.activity :as model.activity]
            [jiksnu.model.user :as model.user])
  (:import org.apache.abdera.model.Entry
           jiksnu.model.Activity
           jiksnu.model.User))

(describe apply-view "#'index :atom"
  (do-it "should be a map"
    (with-serialization :http
      (with-format :atom
       (with-user (model.user/create (factory User))
         (let [activity (model.activity/create (factory Activity))
               response (apply-view {:action #'index
                                     :format :atom} [activity])]
           (expect (map? response))))))))

