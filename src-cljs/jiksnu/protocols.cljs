(ns jiksnu.protocols)

(defprotocol AppProtocol
  (add-stream        [this stream-name])
  (get-user          [this])
  (get-user-id       [this])
  (get-websocket-url [this])
  (go                [this state])
  (login             [this username password])
  (post              [this activity pictures])
  (register          [this params])
  (send              [this command])
  (update-page       [this message])
  (unfollow          [this target]))
