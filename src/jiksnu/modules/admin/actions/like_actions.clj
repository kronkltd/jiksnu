(ns jiksnu.modules.admin.actions.like-actions
  (:require [jiksnu.actions.like-actions :as actions.like]))

(defn index
  [& options]
  (apply actions.like/index options))

(defn delete
  [& options]
  (apply actions.like/delete options))
