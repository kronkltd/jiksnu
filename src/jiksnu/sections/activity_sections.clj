(ns jiksnu.sections.activity-sections
  (:use [ciste.core :only [with-format]]
        [ciste.sections :only [defsection]]
        [ciste.sections.default :only [add-form delete-button edit-button
                                       full-uri index-section show-section-minimal
                                       show-section link-to uri title index-block
                                       index-line index-section update-button]]
        [clojure.core.incubator :only [-?>]]
        [jiksnu.ko :only [*dynamic*]]
        [jiksnu.sections :only [action-link actions-section admin-index-line admin-index-block
                                admin-index-section bind-property bind-to control-line
                                display-property display-timestamp
                                dropdown-menu dump-data format-links pagination-links]]
        [slingshot.slingshot :only [throw+]])
  (:require [ciste.model :as cm]
            [clojure.string :as string]
            [clojure.tools.logging :as log]
            [hiccup.core :as h]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.actions.comment-actions :as actions.comment]
            [jiksnu.model :as model]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.conversation :as model.conversation]
            [jiksnu.model.like :as model.like]
            [jiksnu.model.resource :as model.resource]
            [jiksnu.model.user :as model.user]
            [jiksnu.namespace :as ns]
            [jiksnu.rdf :as rdf]
            [jiksnu.sections.user-sections :as sections.user]
            [jiksnu.session :as session]
            [jiksnu.util :as util]
            [jiksnu.xmpp.element :as element]
            [plaza.rdf.core :as plaza]
            [ring.util.codec :as codec])
  (:import java.io.StringWriter
           javax.xml.namespace.QName
           jiksnu.model.Activity
           jiksnu.model.Conversation
           jiksnu.model.Resource
           jiksnu.model.User
           org.apache.abdera.model.Entry
           org.apache.abdera.model.ExtensibleElement))

(defn index-formats
  [activities]
  (map
   (fn [[f h]]
     (let [def (format-links f)]
       (merge def
              {:href h})))
   [[:as        "/api/statuses/public_timeline.as"]
    [:atom      "/api/statuses/public_timeline.atom"]
    [:json      "/api/statuses/public_timeline.json"]
    [:n3        "/api/statuses/public_timeline.n3"]
    [:rdf       "/api/statuses/public_timeline.rdf"]
    [:viewmodel "/api/statuses/public_timeline.viewmodel"]
    [:xml       "/api/statuses/public_timeline.xml"]]))

(defn timeline-formats
  [user]
  (map
   (fn [[f h]]
     (let [def (format-links f)]
       (merge def
              {:href h})))
   [[:json (sections.user/user-timeline-link user "json")]
    [:atom (sections.user/user-timeline-link user "atom")]
    [:as   (sections.user/user-timeline-link user "as")]
    [:n3   (sections.user/user-timeline-link user "n3")]
    [:rdf  (sections.user/user-timeline-link user "rdf")]
    [:xml  (sections.user/user-timeline-link user "xml")]]))

;; admin-index-block

(defsection admin-index-block [Activity]
  [items & [page]]
  (map #(admin-index-line % page) items))

(defsection admin-index-block [Activity :viewmodel]
  [items & [page]]
  (->> items
       (map (fn [m] (index-line m page)))
       doall))

;; admin-index-section

(defsection admin-index-section [Activity]
  [items & [page]]
  (admin-index-block items page))

(defsection admin-index-section [Activity :viewmodel]
  [items & [page]]
  (admin-index-block items page))

;; index-block

(defsection index-block [Activity]
  [items & [page]]
  (doall (map #(index-line % page) items)))

(defsection index-block [Activity :viewmodel]
  [items & [page]]
  (->> items
       (map (fn [m] (index-line m page)))
       doall))

(defsection index-block [Activity :xml]
  [activities & _]
  [:statuses {:type "array"}
   (map index-line activities)])

;; index-line

(defsection index-line [Activity]
  [activity & [page]]
  (show-section activity page))

;; index-section

(defsection index-section [Activity]
  [items & [page]]
  (index-block items page))

(defsection show-section [Activity :json]
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
  (dissoc activity :links))

(defsection show-section [Activity :viewmodel]
  [activity & [page]]
  (dissoc activity :links))

(defsection show-section [Activity :xml]
  [activity & _]
  [:status
   [:text (h/h (or (:title activity)
                   (:content activity)))]
   [:truncated "false"]
   [:created_at (-?> activity :published .toDate util/date->twitter)]
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

;; title

(defsection title [Activity]
  [activity & options]
  (:title activity))

;; uri

(defsection uri [Activity]
  [activity & options]
  (str "/notice/" (:_id activity)))
