(ns jiksnu.protocols)

(defprotocol AppProtocol
  (add-stream        [this stream-name])
  (get-websocket-url [this])
  (post              [this activity pictures])
  (register          [this params])
  (update-page       [this message]))
