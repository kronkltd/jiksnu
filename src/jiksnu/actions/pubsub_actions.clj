(ns jiksnu.actions.pubsub-actions
  (:use (ciste [config :only [definitializer]]
               [core :only [defaction]]
               [debug :only [spy]]))
  (:require (aleph [http :as http])
            (clojure [string :as string])
            (clojure.tools [logging :as log])
            (lamina [core :as l])
            (jiksnu [model :as model])
            (jiksnu.actions [feed-source-actions :as actions.feed-source])
            (jiksnu.model [feed-source :as model.feed-source])))

;; TODO: break into components and actually perform validation
(defn valid?
  "How could this ever go wrong?"
  [_] true)

;; TODO: move to sections
(defn subscription-not-valid-error
  "Error response for invalid subscription"
  []
  
  )

;; TODO: move to sections
(defn subscription-not-found-error
  []
  {:mode "error"
   :message "not found"})











(defn verify-subscribe-sync
  "Verify subscription request in this thread"
  [subscription]
  (if (and (valid? (:topic subscription))
           (valid? (:callback subscription)))
    ;; sending verification request
    (let [params (merge {:hub.mode (:mode subscription)
                         :hub.topic (:topic subscription)
                         :hub.lease_seconds (:lease-seconds subscription)
                         :hub.verify_token (:verify-token subscription)}
                        (if (:challenge subscription)
                          {:hub.challenge (:challenge subscription)}))
          url (model/make-subscribe-uri (:callback subscription) params)
          response-channel (http/http-request {:method :get
                                               :url url
                                               :auto-transform true})]
      (let [response @response-channel]
        (if (= 200 (:status response))
          {:status 204}
          {:status 404})))
    (subscription-not-valid-error)))

(defaction verify-subscription-async
  [subscription]
  (l/task
   (verify-subscribe-sync subscription)))

;; TODO: extract hub params in filter
(defaction hub-dispatch
  [params]
  (let [mode (or (get (spy params) :hub.mode) (get params "hub.mode"))
        callback (or (get params :hub.callback) (get params "hub.callback"))
        challenge (or (get params :hub.challenge) (get params "hub.challenge"))
        lease-seconds (or (get params :hub.lease_seconds) (get params "hub.lease_seconds"))
        verify (or (get params :hub.verify) (get params "hub.verify"))
        verify-token (or (get params :hub.verify_token) (get params "hub.verify_token"))
        secret (or (get params :hub.secret) (get params "hub.secret"))
        topic (or (get params :hub.topic) (get params "hub.topic"))]
    (condp = mode
      "subscribe"
      ;; set up feed subscriber
      (let [source (model.feed-source/find-or-create
                    {:topic topic :callback callback}
                    {:mode mode :challenge challenge
                     :verify-token verify-token
                     :lease-seconds lease-seconds})]
        (if (= verify "async")
          (verify-subscription-async source)
          (verify-subscribe-sync source)))

      
      "unsubscribe"
      ;; remove feed subscriber
      (if-let [subscription (model.feed-source/fetch {:topic topic :callback callback})]
        (actions.feed-source/remove-subscription subscription)
        (subscription-not-found-error))
      
      nil)))
