(ns jiksnu.modules.http.resources
  (:require [clojure.tools.logging :as log]
            [octohipster.core :as octo]
            [jiksnu.modules.http.routes :as r]
            )
  )

(defmacro defresource
  [group name & opts]
  `(do
     (declare ~name)
     (log/infof "defining resource: %s" (var ~name))
     (octo/defresource ~name
       ~@opts)

     (dosync
      (alter r/resources assoc-in [(var ~group) (var ~name)] ~name))))


(defmacro defgroup
  [name & opts]
  `(do
     (declare ~name)
     (octo/defgroup ~name
       ~@opts)

     (dosync
      (alter r/groups assoc (var ~name) ~name))))
