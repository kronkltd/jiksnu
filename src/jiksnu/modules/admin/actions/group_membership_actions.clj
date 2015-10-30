(ns jiksnu.modules.admin.actions.group-membership-actions
  (:require [jiksnu.actions.group-membership-actions :as actions.group-membership]))

(defn index
  [& options]
  (apply actions.group-membership/index options))

(defn create
  [params]
  (actions.group-membership/create params))

(defn delete
  [group]
  (actions.group-membership/delete group))

(defn show
  [group]
  (actions.group-membership/show group))
