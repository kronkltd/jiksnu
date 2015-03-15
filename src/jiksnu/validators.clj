(ns jiksnu.validators
  (:require [validateur.validation :refer [acceptance-of]]))

(defn type-of
  [path type]
  (acceptance-of path :accept (partial instance? type)))
