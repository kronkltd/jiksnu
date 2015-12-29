(ns jiksnu.modules.core.sections.layout-sections
  (:require [ciste.core :refer [apply-template]]))

(defmethod apply-template :command
  [request response]
  (let [body (:body response)]
    (assoc response :body
           {:type (get response :type "event")
            :body body})))
