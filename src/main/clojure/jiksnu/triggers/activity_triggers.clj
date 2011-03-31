(ns jiksnu.triggers.activity-triggers)

(defn notify-activity
  [recipient ^Activity activity]
  (with-serialization :xmpp
    (with-format :xmpp
      (let [recipient-jid (make-jid recipient)
            author (model.user/fetch-by-id (first (:authors activity)))
            message-text (:summary activity)
            ele (make-element
                 ["message" {"type" "headline"}
                  ["event" {"xmlns" event-ns}
                   (index-block [activity])]])
            message
            (make-packet
             {:to recipient-jid
              :from (make-jid author)
              :type :chat
              ;; FIXME: generate an id for this case
              :id "JIKSNU1"
              :body ele})]
        (deliver-packet! message)))))

(defn notify-subscribers
  [action request activity]
  (with-database
    (let [user (model.user/fetch-by-id (first (:authors activity)))
          subscribers (conj (model.subscription/subscribers user) user)]
      (doseq [subscription subscribers]
        (model.item/push user activity)
        (notify-activity user activity)))))

(add-trigger! #'jiksnu.http.controller.activity-controller/create
              #'notify-subscribers)
(add-trigger! #'jiksnu.http.controller.activity-controller/create
              #'sleep-and-print)
