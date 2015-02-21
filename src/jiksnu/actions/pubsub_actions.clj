(ns jiksnu.actions.pubsub-actions
  (:require [ciste.core :refer [defaction]]
            [clojure.tools.logging :as log]
            [lamina.core :as l]
            [lamina.executor :as e]
            [jiksnu.actions.feed-source-actions :as actions.feed-source]
            [jiksnu.actions.feed-subscription-actions :as actions.feed-subscription]
            [jiksnu.model :as model]
            [jiksnu.model.feed-source :as model.feed-source]
            [jiksnu.templates.actions :as templates.actions]
            [jiksnu.util :as util]
            [org.httpkit.client :as client]
            [slingshot.slingshot :refer [throw+]]))

(defn verify-subscribe-sync
  "Verify subscription request in this thread"
  [feed-subscription params]
  (if-let [callback (:callback params)]
    (if (:url feed-subscription)
      ;; sending verification request
      (let [params {:hub.mode          "subscribe"
                    :hub.topic         (:url feed-subscription)
                    :hub.lease_seconds (:lease-seconds feed-subscription)
                    :hub.challenge     (:challenge feed-subscription)
                    :hub.verify_token  (:verify-token feed-subscription)}
            url (util/make-subscribe-uri (:callback feed-subscription) params)
            ;; TODO: handle this in resources?
            response-p (client/get url)]
        ;; NB: This blocks
        (let [response @response-p]
          (if (= 200 (:status response))
            {:status 204}
            {:status 404})))
      (throw+ "feed subscription is not valid"))
    (throw+ "Could not determine callback url")))

(defaction verify-subscription-async
  "asynchronous verification of hub subscription"
  [subscription params]
  (verify-subscribe-sync subscription params))

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
