(ns jiksnu.routes.message-routes
  (:use [clojurewerkz.route-one.core :only [add-route! named-path]]
        [jiksnu.routes.helpers :only [formatted-path]])
  (:require [jiksnu.actions.message-actions :as message]))

(defn routes
  []
  [[[:get "/:username/inbox"]  #'message/inbox-page]
   [[:get "/:username/outbox"] #'message/outbox-page]])
