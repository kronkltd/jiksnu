(ns jiksnu.actions.user-list-actions
  (:require [ciste.core :refer [defaction]]
            [ciste.model :as cm]
            [clojure.tools.logging :as log]
            [jiksnu.model :as model]
            [jiksnu.model.group :as model.group]
            [jiksnu.session :as session]
            [jiksnu.templates.actions :as templates.actions]
            [jiksnu.transforms :refer [set-_id set-created-time
                                      set-updated-time]]
            [slingshot.slingshot :refer [throw+]])
  (:import jiksnu.model.UserList))

(defaction user-list
  [user]
  [user {:items []}]
  )