(ns jiksnu.model.group
  (:require (karras [entity :as entity]))
  (:import jiksnu.model.Group))

(defn create
  [options]
  (entity/create Group options))

(defn index
  []
  (entity/fetch-all Group)
  )
