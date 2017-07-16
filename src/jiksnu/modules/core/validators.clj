(ns jiksnu.modules.core.validators)

(defn type-of
  [attr type]
  (let [f (if (vector? attr) get-in get)]
    (fn [m]
      (if (instance? type (f m attr))
        [true {}]
        [false {attr #{(str "is not type: " type)}}]))))
