(ns jiksnu.modules.core.sections.layout-sections
  (:use [ciste.core :only [apply-template]])
  (:require [clojure.tools.logging :as log]))

(defmethod apply-template :command
  [request response]
  (let [body (:body response)]
    (assoc response :body
           {:type (get response :type "event")
            :body body})))
