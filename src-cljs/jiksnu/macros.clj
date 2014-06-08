(ns jiksnu.macros)

(defmacro defvar [name [this & args] & body]
  `(def ~name
     (fn [~@args]
       (cljs.core/this-as ~this
         ~@body
         ~this))))

(defmacro defmodel
  [name stub & defaults]
  `(def ~name
     (let [model# (.-PageModel (.-model js/jiksnu))]
       (.extend model#
                (purnam.core/obj
                 :type ~(str name)
                 :stub ~stub
                 :defaults
                 (fn []
                   (.extend js/_
                            (purnam.core/obj ~@defaults)
                            (.defaults (.-prototype model#)))))))))

(defmacro defcollection
  [name model & options]
  `(def ~name
     (.extend (.-Collection js/Backbone)
              (cljs.core/js-obj
               "type" ~(str name)
               "model" (fn [attrs# options#]
                         (.create ~model attrs# options#))
               ~@options))))
