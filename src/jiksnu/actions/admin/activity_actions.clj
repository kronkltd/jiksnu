(ns
    ^{:doc "This is the namespace for the admin pages for activities"}
  jiksnu.actions.admin.activity-actions
  (:use (ciste [core :only [defaction]]))
  (:require (jiksnu.model [activity :as model.activity])))

(defaction index
  []
  (model.activity/index))
