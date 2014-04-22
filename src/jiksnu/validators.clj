(ns jiksnu.validators
  (:use [validateur.validation :only [acceptance-of]]))

(defn type-of
  [path type]
  (acceptance-of path :accept (partial instance? type)))
