(ns jiksnu.protocols)

(defprotocol AppProtocol
  (add-stream        [this stream-name])
  (connect           [this])
  (delete-stream     [this target-id])
  (fetch-status      [this])
  (follow            [this target])
  (following?        [this target])
  (get-user          [this])
  (get-user-id       [this])
  (get-websocket-url [this])
  (go                [this state])
  (handle-message    [this message])
  (login             [this username password])
  (logout            [this])
  (ping              [this])
  (post              [this activity pictures])
  (refresh           [this])
  (register          [this params])
  (send              [this command])
  (update-page       [this message])
  (unfollow          [this target]))