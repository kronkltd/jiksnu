(ns jiksnu.modules.http.resources
  (:require [octohipster.core :as octo]
            [jiksnu.modules.http.routes :as r]
            )
  )

(defmacro defresource
  [group name & {:as opts}]
  `(do
     (octo/defresource ~name
       ~@opts
       )

     (dosync
      (alter r/resources assoc-in [group name] ~name)
      )
     )
  )


(defmacro defgroup
  [name & {:as opts}]
  `(do
     (octo/defgroup ~name
       ~@opts
       )

     (dosync
      (alter r/groups assoc ~'name ~name)
      )
     )
  )
