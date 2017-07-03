(ns jiksnu.app.actions
  (:require [jiksnu.app.protocols :as p]
            [jiksnu.app.provider-methods :as methods]))

(defmethod methods/handle-action "page-add"
  [app data]
  (p/update-page app data))
