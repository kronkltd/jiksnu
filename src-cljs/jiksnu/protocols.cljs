(ns jiksnu.protocols)

(defprotocol AppProtocol
  (add-stream        [this stream-name])
  (get-websocket-url [this])
  (login             [this username password])
  (post              [this activity pictures])
  (register          [this params])
  (send              [this command])
  (update-page       [this message])
  (unfollow          [this target]))
