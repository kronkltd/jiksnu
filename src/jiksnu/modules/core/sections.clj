(ns jiksnu.modules.core.sections
  (:require [ciste.config :refer [config]]
            [ciste.core :refer [apply-template with-format]]
            [ciste.sections :refer [declare-section defsection]]
            [ciste.sections.default :refer [full-uri index-block index-line index-section link-to
                                            show-section title uri]]
            [hiccup.core :as h]
            [inflections.core :as inf]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.namespace :as ns]
            [jiksnu.util :as util])
  (:import jiksnu.model.Activity
           jiksnu.model.Domain))

(defsection full-uri :default
  [record & options]
  (str "http://" (config :domain)
       (apply uri record options)))

(defsection index-block :default
  [items & [page]]
  (map #(index-line % page) items))

(defsection index-line :default
  [item & [page]]
  (show-section item page))

(defsection index-section :default
  [items & [page]]
  (index-block items page))

;; TODO: only for html
(defsection link-to :default
  [record & options]
  (let [options-map (apply hash-map options)]
    [:a {:href (uri record)}
     [:span {:about (uri record)
             :property "dc:title"}
      (or (:title options-map) (title record))]]))

(defsection title :default
  [record & _]
  (str (:_id record)))

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

(defsection show-section [Domain :jrd]
  [item & [page]]
  (let [id (:_id item)
        template (format "http://%s/main/xrd?uri={uri}" id)]
    {:host id
     :links [{:template template
              :rel "lrdd"
              :title "Resource Descriptor"}]}))

(defsection show-section [Domain :xrd]
  [item & [page]]
  (let [id (:_id item)]
    ["XRD" {"xmlns" ns/xrd
            "xmlns:hm" ns/host-meta}
     ["hm:Host" id]
     ["Subject" id]
     (map
      ;; TODO: show-section [Link :xrd]
      (fn [{:keys [title rel href template] :as link}]
        [:Link (merge {}
                      (when rel {:rel rel})
                      (when href {:href href})
                      (when template {:template template}))
         (when title [:Title title])])
      (:links item))]))

(defmethod apply-template :command
  [request response]
  (let [body (:body response)]
    (assoc response :body
           {:type (get response :type "event")
            :body body})))
