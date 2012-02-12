(ns jiksnu.actions.feed-source-actions
  (:use (ciste [config :only [config definitializer]]
               [core :only [defaction]]
               [debug :only [spy]])
        (jiksnu model session)
        (karras [entity :only [make]]))
  (:require (aleph [http :as http])
            (clojure [string :as string])
            (jiksnu.helpers [user-helpers :as helpers.user])
            (jiksnu.model [push-subscription :as model.push])
            (lamina [core :as l])))

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


;; TODO: special case local subscriptions
(defaction subscribe
  
  [user]
  (if-let [hub-url (:hub user)]
    (let [topic (helpers.user/feed-link-uri user)]
      (model.push/find-or-create {:topic topic :hub hub-url})
      (let [subscribe-link
            (make-subscribe-uri
             hub-url
             {:hub.callback (str "http://" (config :domain) "/main/push/callback")
              :hub.mode "subscribe"
              :hub.topic topic
              :hub.verify "async"})]
        (http/sync-http-request
         {:method :get
          :url subscribe-link
          :auto-transform true})))))

(defn remove-subscription
  [subscription])

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
