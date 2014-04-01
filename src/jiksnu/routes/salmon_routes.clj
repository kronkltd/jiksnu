(ns jiksnu.routes.salmon-routes
  (:require [jiksnu.actions.salmon-actions :as salmon]
            [jiksnu.routes.helpers :refer [add-route! named-path]]))

(add-route! "/main/salmon/user/:id" {:named "user salmon"})

(defn routes
  []
  [[[:post (named-path "user salmon")] #'salmon/process]])

