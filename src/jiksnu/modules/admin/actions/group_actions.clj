(ns jiksnu.modules.admin.actions.group-actions
  (:require [jiksnu.actions.group-actions :as actions.group]))

(defn index
  [& options]
  (apply actions.group/index options))

(defn create
  [params]
  (actions.group/create params))

(defn delete
  [group]
  (actions.group/delete group))

(defn show
  [group]
  (actions.group/show group))
