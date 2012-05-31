(ns jiksnu.actions.feed-source-actions
  (:use (ciste [config :only [config definitializer]]
               [core :only [defaction]]
               [debug :only [spy]]
               [runner :only [require-namespaces]])
        (karras [entity :only [make]]))
  (:require (aleph [http :as http])
            (ciste [model :as cm])
            (clj-http [client :as client])
            (clojure [string :as string])
            (clojure.tools [logging :as log])
            (jiksnu [model :as model])
            (jiksnu.helpers [user-helpers :as helpers.user])
            (jiksnu.model [feed-source :as model.feed-source])
            (lamina [core :as l])))

(defaction confirm
  "Callback for when a remote subscription has been confirmed"
  [source]
  (model.feed-source/update-field! source :status "confirmed"))

(defaction process-updates
  [params]
  (let [{challenge "hub.challenge"
         mode "hub.mode"
         topic "hub.topic"} params]
    (let [sources (model.feed-source/fetch-all {:topic topic})]
      (condp = mode
        "subscribe" (do
                      (cm/implement (log/info "confirming subscription")))

        "unsubscribe" (do
                        (cm/implement (log/info "confirming subscription removal"))
                        (doseq [source sources]
                          (model.feed-source/delete source)))
       (cm/implement
        (log/info "Unknown mode"))))
    challenge))

(defn find-or-create
  [search-params update-params]
  ;; FIXME: no find or creates in model
  (model.feed-source/find-or-create (merge search-params update-params)))


;; TODO: special case local subscriptions
;; TODO: should take a source
(defaction subscribe
  "Send a subscription request to the feed"
  [user]
  (if-let [hub-url (:hub user)]
    (let [topic (helpers.user/feed-link-uri user)]
      (find-or-create {:topic topic :hub hub-url} {})
      (client/post
       hub-url
       {:throw-exceptions false
        :form-params
        {"hub.callback" (str "http://" (config :domain) "/main/push/callback")
         "hub.mode" "subscribe"
         "hub.topic" topic
         "hub.verify" "async"}}))))

(defn send-unsubscribe
  ([hub topic]
     (send-unsubscribe
      hub topic
      (str "http://" (config :domain) "/main/push/callback")))
  ([hub topic callback]
     (client/post
      hub
      {:throw-exceptions false
       :form-params
       {"hub.callback" callback
        "hub.mode" "unsubscribe"
        "hub.topic" topic
        "hub.verify" "async"}})))

;; TODO: Rename to unsubscribe and make an action
(defaction remove-subscription
  [subscription]
  (send-unsubscribe
   (:hub subscription)
   (:topic subscription))
  true)

(definitializer
  (require-namespaces
   ["jiksnu.filters.feed-source-filters"
    "jiksnu.views.feed-source-views"]))
