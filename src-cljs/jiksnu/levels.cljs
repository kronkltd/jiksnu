(ns jiksnu.levels)

(def logging-levels
  {
   ;; "waltz.state"        :finest
    "jiksnu.core"        :fine
    "jiksnu.events"    :finer
    "jiksnu.model"       :fine
    "jiksnu.routes" :finest
    "jiksnu.websocket"   :warning
    "goog.net.WebSocket" :warning
   })

