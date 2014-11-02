(ns jiksnu.levels)

(def logging-levels
  {
   ;; "waltz.state"        :finest
    "jiksnu.core"        :finest
    "jiksnu.events"      :info
    "jiksnu.model"       :info
    "jiksnu.routes"      :finest
    "jiksnu.websocket"   :info
    "goog.net.WebSocket" :warning
   })

