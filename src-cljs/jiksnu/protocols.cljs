(ns jiksnu.protocols)

(defprotocol AppProtocol
  (add-stream        [this stream-name])
  (get-websocket-url [this])
  (register          [this params])
  (update-page       [this message]))
