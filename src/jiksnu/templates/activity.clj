(ns jiksnu.templates.activity
  (:use (ciste [debug :only (spy)])
        (closure.templates [core :only (deftemplate)]))
  (:require (jiksnu.model [activity :as model.activity]
                          [user :as model.user])))

(deftemplate show
  [activity]
  (model.activity/format-data activity))

(deftemplate index-block
  [activities]
  {:activities (map model.activity/format-data activities)})

(deftemplate user-timeline
  [user activities]
  {:user (model.user/format-data user)
   :activities (map model.activity/format-data activities)})
