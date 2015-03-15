(ns jiksnu.modules.web.transforms.conversation-transforms)

(defn set-url
  [item]
  (if (:url item)
    item
    (when (:local item)
      (assoc item :url ""))))

