(ns jiksnu.modules.core.sections.activity-sections
  (:require [ciste.sections :refer [defsection]]
            [ciste.sections.default :refer [edit-button
                                            show-section-minimal
                                            show-section uri title index-block
                                            index-line index-section update-button]]
            [clojure.string :as string]
            [clojure.tools.logging :as log]
            [hiccup.core :as h]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.namespace :as ns]
            [jiksnu.modules.core.sections :refer [admin-index-line admin-index-block
                                                  admin-index-section ]]
            [jiksnu.modules.core.sections.user-sections :as sections.user]
            [jiksnu.util :as util]
            [slingshot.slingshot :refer [throw+]])
  (:import jiksnu.model.Activity))

(defsection admin-index-block [Activity]
  [items & [page]]
  (map #(admin-index-line % page) items))

(defsection admin-index-section [Activity]
  [items & [page]]
  (admin-index-block items page))

(defsection index-block [Activity]
  [items & [page]]
  (doall (map #(index-line % page) items)))

(defsection index-block [Activity :xml]
  [activities & _]
  [:statuses {:type "array"}
   (map index-line activities)])

(defsection index-line [Activity]
  [activity & [page]]
  (show-section activity page))

(defsection index-section [Activity]
  [items & [page]]
  (index-block items page))

(defsection show-section [Activity :twitter]
  [activity & _]
  (merge
   {:text (:title activity)
    :truncated false
    :created_at (util/date->twitter (.toDate (:published activity)))
    :source (:source activity)
    :id (:_id activity)
    ;; :in_reply_to_user_id nil
    ;; :in_reply_to_screen_name nil

    ;; TODO: test for the presence of a like
    :favorited false
    :user (let [user (model.activity/get-author activity)]
            (show-section user))
    :statusnet_html (:content activity)}
   (when-let [conversation (first (:conversation-uris activity))]
     {:statusnet_conversation_id conversation})
   (let [irt (first (:irts activity))]
     {:in_reply_to_status_id irt})
   (when-let [attachments (:attachments activity)]
     {:attachments attachments})))

(defsection show-section [Activity :model]
  [activity & [page]]

  ;; (dissoc activity :links)
  activity
  )

(defsection show-section [Activity :xml]
  [activity & _]
  [:status
   [:text (h/h (or (:title activity)
                   (:content activity)))]
   [:truncated "false"]
   [:created_at (some-> activity :published .toDate util/date->twitter)]
   [:source (:source activity)]
   [:id (:_id activity)]
   [:in_reply_to_status_id]
   [:in_reply_to_user_id]
   [:favorited "false" #_(liked? (current-user) activity)]
   [:in_reply_to_screen_name]
   (show-section (model.activity/get-author activity))
   (when (:geo activity)
     (list [:geo]
           [:coordnates]
           [:place]))
   [:contributors]
   [:entities
    [:user_mentions
     ;; TODO: list mentions
     ]
    [:urls
     ;; TODO: list urls
     ]
    [:hashtags
     ;; TODO: list hashtags
     ]]])

(defsection title [Activity]
  [activity & options]
  (:title activity))

(defsection uri [Activity]
  [activity & options]
  (str "/notice/" (:_id activity)))
