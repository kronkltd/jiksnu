(ns jiksnu.modules.admin.actions.group-membership-actions
  (:use [ciste.core :only [defaction]])
  (:require [taoensso.timbre :as log]
            [jiksnu.actions.group-membership-actions :as actions.group-membership]))

(defaction index
  [& options]
  (apply actions.group-membership/index options))

(defaction create
  [params]
  (actions.group-membership/create params))

(defaction delete
  [group]
  (actions.group-membership/delete group))

(defaction show
  [group]
  (actions.group-membership/show group))
