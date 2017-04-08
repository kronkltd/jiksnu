(ns jiksnu.protocols)

(defprotocol AppProtocol
  (add-stream [this stream-name]))
