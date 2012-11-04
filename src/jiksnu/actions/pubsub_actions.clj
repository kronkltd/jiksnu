(ns jiksnu.actions.pubsub-actions
  (:use [ciste.initializer :only [definitializer]]
        [ciste.core :only [defaction]]
        [ciste.loader :only [require-namespaces]]
        [slingshot.slingshot :only [throw+]])
  (:require [aleph.http :as http]
            [clojure.string :as string]
            [clojure.tools.logging :as log]
            [lamina.core :as l]
            [lamina.executor :as e]
            [jiksnu.actions.feed-source-actions :as actions.feed-source]
            [jiksnu.model :as model]
            [jiksnu.model.feed-source :as model.feed-source]))

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
  (e/task
   (verify-subscribe-sync subscription)))

(defaction subscribe
  [params]
  ;; set up feed subscriber
  (let [source (actions.feed-source/find-or-create params)]
    (if (= (:verify params) "async")
      (verify-subscription-async source)
      (verify-subscribe-sync source))))

(defaction unsubscribe
  [params]
  ;; remove feed subscriber
  (if-let [subscription (model.feed-source/find-record {:topic (:topic params)
                                                        :callback (:callback params)})]
    (actions.feed-source/remove-subscription subscription)
    (subscription-not-found-error)))

;; TODO: extract hub params in filter
(defaction hub-dispatch
  [params]
  (condp = (:mode params)
    "subscribe"   (subscribe params)
    "unsubscribe" (unsubscribe params)
    (throw+ "Unknown mode type")))

(definitializer
  (require-namespaces
   ["jiksnu.filters.pubsub-filters"
    ;; "jiksnu.triggers.pubsub-triggers"
    "jiksnu.views.pubsub-views"]))
