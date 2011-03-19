(ns jiksnu.http.view.activity-view-test
  (:use ciste.core
        ciste.factory
        ciste.view
        jiksnu.config
        jiksnu.http.controller.activity-controller
        jiksnu.http.view
        jiksnu.http.view.activity-view
        jiksnu.model
        jiksnu.session
        jiksnu.view
        [lazytest.describe :only (describe do-it testing)]
        [lazytest.expect :only (expect)])
  (:require [jiksnu.model.activity :as model.activity]
            [jiksnu.model.user :as model.user])
  (:import jiksnu.model.Activity
           jiksnu.model.User))

(describe uri "Activity"
  (do-it "should be a string"
    (with-serialization :http
      (with-format :html
        (with-user (model.user/create (factory User))
          (let [activity (model.activity/create (factory Activity))]
            (expect (string? (uri activity)))))))))

(describe add-form "Activity"
  (do-it "should be a vector"
    (with-serialization :http
      (with-format :html
        (with-user (model.user/create (factory User))
          (let [activity (model.activity/create (factory Activity))]
            (expect (vector? (add-form activity)))))))))

(describe show-section-minimal "[Activity :html]"
  (do-it "should be a vector"
    (with-serialization :http
      (with-format :html
        (with-user (model.user/create (factory User))
          (let [activity (model.activity/create (factory Activity))]
            (expect (vector? (show-section-minimal activity)))))))))

(describe edit-form
  (do-it "should be a vector"
    (with-serialization :http
      (with-format :html
        (with-user (model.user/create (factory User))
          (let [activity (model.activity/create (factory Activity))]
            (expect (vector? (edit-form activity)))))))))

(describe index-line-minimal
  (do-it "should be a vector"
    (with-serialization :http
      (with-format :html
        (with-user (model.user/create (factory User))
          (let [activity (model.activity/create (factory Activity))]
            (expect (vector? (index-line-minimal activity)))))))))

(describe index-block-minimal
  (do-it "should be a vector"
    (with-serialization :http
      (with-format :html
        (with-user (model.user/create (factory User))
          (let [activity (model.activity/create (factory Activity))]
            (expect (vector? (index-block-minimal [activity])))))))))

(describe apply-view "#'index :atom"
  (do-it "should be a map"
    (with-serialization :http
      (with-format :atom
       (with-user (model.user/create (factory User))
         (let [activity (model.activity/create (factory Activity))
               response (apply-view {:action #'index
                                     :format :atom} [activity])]
           (expect (map? response))))))))
