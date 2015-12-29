(ns jiksnu.actions.user-list-actions
  (:require [jiksnu.model :as model]
            [jiksnu.model.group :as model.group]
            [jiksnu.templates.actions :as templates.actions]
            [jiksnu.transforms :refer [set-_id set-created-time
                                      set-updated-time]]
            [slingshot.slingshot :refer [throw+]])
  (:import jiksnu.model.UserList))

(defn user-list
  [user]
  [user {:items []}])
