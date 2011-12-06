(ns jiksnu.templates.activity
  (:use (ciste [debug :only [spy]])
        (clojure.core [incubator :onlt [-?>]])
        (closure.templates [core :only [deftemplate]])
        (jiksnu [session :only [current-user]]))
  (:require (jiksnu.model [activity :as model.activity]
                          [user :as model.user])))

(deftemplate show
  [activity]
  (let [data (model.activity/format-data activity)]
    (if-let [current (current-user)]
      (assoc data :authenticated (model.user/format-data current))
      data)))

(deftemplate index-block
  [activities]
  {:activities (map model.activity/format-data activities)
   :authenticated (-?> (current-user) model.user/format-data )})

(deftemplate user-timeline
  [user activities]
  {:user (model.user/format-data user)
   :authenticated (-?> (current-user) model.user/format-data )
   :activities (map model.activity/format-data activities)})
