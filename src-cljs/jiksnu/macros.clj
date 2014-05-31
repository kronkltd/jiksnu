(ns jiksnu.macros)

(defmacro defvar [name [this & args] & body]
  `(def ~name
     (fn [~@args]
       (cljs.core/this-as ~this
                ~@body
                ~this))))

