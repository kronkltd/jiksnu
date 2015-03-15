(ns jiksnu.modules.web.transforms.activity-transforms
  (:require [slingshot.slingshot :refer [throw+]]))

(defn set-url
  [activity]
  (if (seq (:url activity))
    activity
    (if (:local activity)
      (assoc activity :url "")
      (if (:id activity)
        (assoc activity :url (:id activity))
        (throw+ "Could not determine activity url")))))
