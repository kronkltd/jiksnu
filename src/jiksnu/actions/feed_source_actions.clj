(ns jiksnu.actions.feed-source-actions
  (:use (ciste [config :only [config definitializer]]
               [core :only [defaction]]
               [debug :only [spy]])
        (jiksnu model session)
        (karras [entity :only [make]]))
  (:require (aleph [http :as http])
            (clojure [string :as string])
            (jiksnu.helpers [user-helpers :as helpers.user])
            (jiksnu.model [feed-source :as model.feed-source])
            (lamina [core :as l])))

(defaction callback
  [params]
  (let [{{challenge :hub.challenge
          topic :hub.topic} :params} params]
    challenge))

(defaction admin-index
  [options]
  (model.feed-source/index))


;; TODO: special case local subscriptions
(defaction subscribe
  
  [user]
  (if-let [hub-url (:hub user)]
    (let [topic (helpers.user/feed-link-uri user)]
      (model.feed-source/find-or-create {:topic topic :hub hub-url})
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
  (doseq [namespace ['jiksnu.filters.feed-source-filters
                     'jiksnu.views.feed-source-views]]
    (require namespace)))
