(ns jiksnu.modules.core.formats
  (:require [ciste.core :refer [with-format]]
            [ciste.formats :refer [format-as]]))

(defmethod format-as :as
  [format request response]
  (with-format :json (format-as :json request response)))

;; (defmethod format-as :default
;;   [format request response]
;;   response)

(defmethod format-as :page
  [format request response]
  response)
