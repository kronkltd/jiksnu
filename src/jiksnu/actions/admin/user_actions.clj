(ns
    ^{:doc "This is the namespace for the admin pages for users"}
  jiksnu.actions.admin.user-actions
  (:use (ciste [core :only [defaction]]))
  (:require (jiksnu.actions [user-actions :as actions.user])
            (jiksnu.model [user :as model.user])))

(defaction create
  [options]
  (actions.user/create options))

(defaction index
  [options]
  (model.user/fetch-all))
