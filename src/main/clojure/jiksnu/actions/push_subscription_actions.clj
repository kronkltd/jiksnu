(ns jiksnu.actions.push-subscription-actions
  (:use [ciste core debug]
        [jiksnu model namespace session]
        [karras.entity :only (make)])
  (:require [aleph.http :as http]
            [jiksnu.abdera :as abdera]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.model.push-subscription :as model.push]
            [jiksnu.helpers.activity-helpers :as helpers.activity]
            [jiksnu.helpers.user-helpers :as helpers.user]
            [clojure.java.io :as io]
            [clojure.string :as string]
            jiksnu.view)
  (:import jiksnu.model.Activity
           org.apache.abdera.model.Entry))

(defaction callback
  [params]
  (let [{{challenge :hub.challenge
          topic :hub.topic} :params} params]
    challenge))

(defaction callback-publish
  [params]
  (let [document (spy (abdera/parse-stream (:body params)))
        feed (spy (.getRoot document))
        entries (spy (.getEntries feed))]
    (doseq [entry entries]
      (let [activity (helpers.activity/to-activity entry feed)]
        (actions.activity/create (spy activity))))))

(defaction index
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
  (let [hub-url (:hub user)
        topic (helpers.user/feed-link-uri user)]
    (model.push/find-or-create {:topic topic :hub hub-url})
    (let [subscribe-link
          (make-subscribe-uri
           hub-url
           {:hub.callback "http://beta.jiksnu.com/main/push/callback"
            :hub.mode "subscribe"
            :hub.topic topic
            :hub.verify "async"})]

      (let [response-channel
            (http/http-request
             {:method :get
              :url subscribe-link
              :auto-transform true})]
        @response-channel))))

(defaction hub
  [params]
  (let [{mode :hub.mode
         callback :hub.callback
         verify :hub.verify
         verify-token :hub.verify_token
         secret :hub.secret
         topic :hub.topic} params]
    (if (= mode "subscribe")
      (let [options {:callback callback
                     :secret secret
                     :verify-token verify-token
                     :topic topic}]
        (let [push-subscription
              (model.push/find-or-create {:topic topic
                                          :callback callback})]
          ;; Subscription already exists, update record
          (model.push/update push-subscription)))
      ;; some other options
      )))

(defaction hub-publish
  []
  true)
