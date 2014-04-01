(ns jiksnu.routes.message-routes
  (:require [jiksnu.actions.message-actions :as message]
            [jiksnu.routes.helpers :refer [add-route! named-path]]))

(defn routes
  []
  [[[:get "/:username/inbox"]  #'message/inbox-page]
   [[:get "/:username/outbox"] #'message/outbox-page]])

