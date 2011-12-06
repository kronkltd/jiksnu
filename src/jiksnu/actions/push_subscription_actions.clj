(ns jiksnu.actions.push-subscription-actions
  (:use (ciste [config :only [definitializer]]
               [core :only [defaction]]
               [debug :only [spy]])
        (jiksnu model session)
        (karras [entity :only [make]]))
  (:require (aleph [http :as http])
            (clojure [string :as string])
            (jiksnu [abdera :as abdera])
            (jiksnu.actions [activity-actions :as actions.activity])
            (jiksnu.helpers [activity-helpers :as helpers.activity]
                            [user-helpers :as helpers.user])
            (jiksnu.model [push-subscription :as model.push])
            (lamina [core :as l]))
  (:import jiksnu.model.Activity
           org.apache.abdera2.model.Entry))

(defaction callback
  [params]
  (let [{{challenge :hub.challenge
          topic :hub.topic} :params} params]
    challenge))

(defaction admin-index
  [options]
  (model.push/index))

(defn make-subscribe-uri
  [url options]
  (str url "?"
       (string/join
        "&"
        (map
         (fn [[k v]] (str (name k) "=" v))
         options))))

(defaction subscribe
  [user]
  (if-let [hub-url (:hub user)]
    (let [topic (helpers.user/feed-link-uri user)]
      (model.push/find-or-create {:topic topic :hub hub-url})
      (let [subscribe-link
            (make-subscribe-uri
             hub-url
             {:hub.callback "http://beta.jiksnu.com/main/push/callback"
              :hub.mode "subscribe"
              :hub.topic topic
              :hub.verify "async"})]
        (http/sync-http-request
         {:method :get
          :url (spy subscribe-link)
          :auto-transform true})))))

(defn valid?
  "How could this ever go wrong?"
  [_] true)

(defn subscription-not-valid-error
  []
  
  )

(defn subscription-not-found-error
  []
  {:mode "error"
   :message "not found"})

(defn sync-verify-subscribe
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
          url (make-subscribe-uri (:callback subscription) params)
          response-channel (http/http-request {:method :get
                                               :url url
                                               :auto-transform true})]
      (let [response @response-channel]
        (if (= 200 (:status response))
          {:status 204}
          {:status 404})))
    (subscription-not-valid-error)))

(defn async-verify-subscribe
  [subscription]
  (l/task
   (sync-verify-subscribe subscription)))

(defn remove-subscription
  [subscription])

(defn hub-dispatch
  [params]
  (let [{mode :hub.mode
         callback :hub.callback
         challenge :hub.challenge
         lease-seconds :hub.lease_seconds
         verify :hub.verify
         verify-token :hub.verify_token
         secret :hub.secret
         topic :hub.topic} params]
    (if (= mode "subscribe")
      (let [push-subscription
            (model.push/find-or-create {:topic topic
                                        :callback callback})
            merged-subscription (merge push-subscription
                                       {:mode mode
                                        :challenge challenge
                                        :verify-token verify-token
                                        :lease-seconds lease-seconds})]
        (if (= verify "async")
          (async-verify-subscribe merged-subscription)
          (sync-verify-subscribe merged-subscription)))
      (if (= mode "unsubscribe")
        (if-let [subscription (model.push/fetch {:topic topic
                                                 :callback callback})]
          (remove-subscription subscription)
          (subscription-not-found-error))))))


(defaction hub
  [params]
  (hub-dispatch params))

(defaction hub-publish
  [params]
  (hub-dispatch params))

(definitializer
  (doseq [namespace ['jiksnu.filters.push-subscription-filters
                     'jiksnu.views.push-subscription-views]]
    (require namespace)))
