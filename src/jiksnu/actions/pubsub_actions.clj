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
            [jiksnu.actions.feed-subscription-actions :as actions.feed-subscription]
            [jiksnu.model :as model]
            [jiksnu.model.feed-source :as model.feed-source]))

(defn verify-subscribe-sync
  "Verify subscription request in this thread"
  [source params]
  (if-let [callback (:callback params)]
    (if (:topic source)
      ;; sending verification request
      (let [params (merge {:hub.mode          (:mode source)
                           :hub.topic         (:topic source)
                           :hub.lease_seconds (:lease-seconds source)
                           :hub.verify_token  (:verify-token source)}
                          (if (:challenge source)
                            {:hub.challenge (:challenge source)}))
            url (model/make-subscribe-uri (:callback params) params)
            ;; TODO: handle this in resources?
            response-channel (http/http-request {:method :get
                                                 :url (log/spy url)
                                                 :auto-transform true})]
        (let [response @response-channel]
          (if (= 200 (:status response))
            {:status 204}
            {:status 404})))
      (throw+ "feed source is not valid"))
    (throw+ "Could not determine callback url")))

(defaction verify-subscription-async
  "asynchronous verification of hub subscription"
  [subscription params]
  (e/task
   (verify-subscribe-sync subscription)))

(defaction subscribe
  "Set up a remote subscription to a local source"
  [params]
  (let [subscription (actions.feed-subscription/subscription-request params)
        dispatch-fn (if (= (:verify params) "async")
                      verify-subscription-async
                      verify-subscribe-sync)]
    (dispatch-fn subscription params)))

(defaction unsubscribe
  "Remove a remote subscription to a local source"
  [params]
  ;; TODO: This should be doing a fsub removal
  (if-let [subscription (model.feed-source/find-record {:topic (:topic params)
                                                        :callback (:callback params)})]
    (actions.feed-source/unsubscribe subscription)
    (throw+ "subscription not found")))

(defaction hub-dispatch
  "Handle pubsub requests against hub endpoint"
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
