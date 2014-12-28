(ns jiksnu.providers
  (:require jiksnu.app)
  (:use-macros [gyr.core :only [def.provider]]
               [purnam.core :only [? ?> ! !> f.n def.n do.n
                                   obj arr def* do*n def*n f*n]]))

(def.provider jiksnu.app
  []
  (let [foo "bar"]
   (obj
    :foo foo
    :$get (fn []
            (.log js/console "calling $get")
            )
    )))
