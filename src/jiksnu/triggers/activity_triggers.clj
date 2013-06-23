(ns jiksnu.triggers.activity-triggers
  (:use [ciste.config :only [config]]
        [ciste.triggers :only [add-trigger!]]
        ciste.sections.default
        jiksnu.actions.activity-actions)
  (:require [clj-tigase.core :as tigase]
            [clj-tigase.element :as element]
            [clojure.tools.logging :as log]
            [jiksnu.abdera :as abdera]
            [jiksnu.model :as model]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.actions.conversation-actions :as actions.conversation]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.channels :as ch]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.conversation :as model.conversation]
            [jiksnu.model.domain :as model.domain]
            [jiksnu.model.item :as model.item]
            [jiksnu.model.subscription :as model.subscription]
            [jiksnu.model.user :as model.user]
            [jiksnu.ops :as ops]
            [jiksnu.util :as util]
            [lamina.core :as l])
  (:import java.net.URI
           jiksnu.model.Activity
           jiksnu.model.User))

(defn notify-activity
  [recipient ^Activity activity]
  (log/info (str "Sending notice to: " (model.user/get-uri recipient false)))
  (let [author (model.activity/get-author activity)
        ele (element/make-element
             ["body" {}
              (str (model.user/get-uri author false) ":  "
                   (:title activity))])
        message (tigase/make-packet {:to (tigase/make-jid (:username recipient) (:domain recipient))
                                     :from (tigase/make-jid "updates" (config :domain))
                                     :type :chat
                                     ;; FIXME: generate an id for this case
                                     :id "JIKSNU1"
                                     :body ele})]
    (tigase/deliver-packet! message)))

(defn create-trigger
  [m]
  (log/info "Create Trigger")
  (if-let [activity (:records m)]
    (let [author (model.activity/get-author activity)]
      ;; Add item to author's stream
      (model.item/push author activity)

      (let [conversation (model.conversation/fetch-by-id (:conversation activity))]
        (actions.conversation/add-activity conversation activity))

      ;; Add as a comment to parent posts
      ;; TODO: deprecated
      #_(if-let [parent (model.activity/fetch-by-id (:parent activity))]
          (model.activity/add-comment parent activity))

      ;; Add as comment to irts
      ;; (doseq [parent parent-activities]
      ;;   (model.activity/add-comment parent activity))

      ;; notify users
      (let [mentioned-users (map model.user/fetch-by-id (filter identity (:mentioned activity)))
            to-notify (->> (model.subscription/subscribers author)
                           (map model.subscription/get-actor)
                           (concat mentioned-users)
                           (filter :local)
                           (into #{}))]
        (doseq [user to-notify]
          (notify-activity user activity)))

      ;; TODO: ping feed subscriptions
      )))

(defn init-receivers
  []

  (l/receive-all
   ch/posted-activities
   create-trigger)

  )

(defonce receivers (init-receivers))
