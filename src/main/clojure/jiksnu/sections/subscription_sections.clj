(defsection uri [Subscription]
  [subscription & options]
  (str "/subscriptions/" (:_id subscription)))

(defsection title [Subscription]
  [subscription & options]
  (:to subscription))

(defn delete-form
  [subscription]
  (f/form-to [:delete (uri subscription)]
             (f/hidden-field :id (:_id subscription))
             (f/submit-button "Delete")))

(defsection index-line [Subscription :html]
  [subscription & options]
  [:tr
   [:td (link-to subscription)]
   [:td (link-to (model.user/fetch-by-id (:from subscription)))]
   [:td (link-to (model.user/fetch-by-id (:to subscription)))]
   [:td (:pending subscription)]
   [:td (:created subscription)]
   [:td (delete-form subscription)]])

(defsection index-block [Subscription :html]
  [subscriptions & options]
  [:table
    [:thead
     [:tr
      [:td "Id"]
      [:td "From"]
      [:td "To"]
      [:td "Pending"]
      [:td "Created"]]]
    [:tbody
     (map index-line subscriptions)]])

(defsection index-section [Subscription :html]
  [subscriptions & options]
  [:div
   [:h2 "Subscriptions"]
   (index-block subscriptions)
   (dump subscriptions)])


(defn subscriber-response-element
  [subscription]
  (let [subscriber (model.user/fetch-by-id (:from subscription))]
    ["subscriber" {"node" microblog-uri
                   "created" (format-date (:created subscription))
                   "jid" (str (:username subscriber) "@"
                              (:domain subscriber))}]))

(defn subscription-request-minimal
  [subscription]
  (let [subscribee (model.user/fetch-by-id (:from subscription))]
    ["subscribe" {"node" microblog-uri
                  "jid" (make-jid subscribee)}]))

(defn unsubscription-request-minimal
  [subscription]
  (let [subscribee (model.user/fetch-by-id (:from subscription))]
    ["unsubscribe" {"node" microblog-uri
                    "jid" (make-jid subscribee)}]))

(defn subscriber-response-minimal
  [subscribers]
  ["pubsub" {"xmlns" pubsub-uri}
   ["subscribers" {"node" microblog-uri}
    (map subscriber-response-element subscribers)]])

(defn subscription-response-element
  [subscription]
  (let [subscribee (model.user/fetch-by-id (:to subscription))
        created (:created subscription)]
    ["subscription" {"node" microblog-uri
                     "subscription" "subscribed"
                     "created" (format-date created)
                     "jid" (str (:username subscribee) "@"
                                (:domain subscribee))}]))

(defn subscription-response-minimal
  "Returns a response iq packet containing the ids in entries"
  [subscriptions]
  ["pubsub" {"xmlns" pubsub-uri}
   ["subscriptions" {"node" microblog-uri}
    (map subscription-response-element subscriptions)]])

(defn notify-subscribe
  [request subscription]
  (with-serialization :xmpp
    (with-format :xmpp
      (let [user (model.user/fetch-by-id (:from subscription))
            subscribee (model.user/fetch-by-id (:to subscription))
            ele (make-element
                 ["pubsub" {"xmlns" pubsub-uri}
                  (subscription-request-minimal subscription)])
            packet
            (make-packet
             {:body ele
              :type :set
              :id (:id request)
              :from (make-jid user)
              :to (make-jid subscribee)})]
        (.initVars packet)
        (deliver-packet! packet)))))

(defn notify-unsubscribe
  [request subscription]
  (with-serialization :xmpp
    (with-format :xmpp
      (let [user (model.user/fetch-by-id (:from subscription))
            subscribee (model.user/fetch-by-id (:to subscription))
            ele (make-element
                 ["pubsub" {"xmlns" pubsub-uri}
                  (unsubscription-request-minimal subscription)])
            packet
            (make-packet
             {:body ele
              :type :set
              :id (:id request)
              :from (make-jid user)
              :to (make-jid subscribee)})]
        (.initVars packet)
        (deliver-packet! packet)))))

