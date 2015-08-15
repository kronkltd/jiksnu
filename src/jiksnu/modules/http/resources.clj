(ns jiksnu.modules.http.resources
  (:require [clojure.tools.logging :as log]
            [jiksnu.modules.http.routes :as r]
            [octohipster.core :as octo]
            )
  )

(defmacro defresource
  [group name & opts]
  `(do
     (declare ~name)
     (log/debugf "defining resource: %s" (var ~name))
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

