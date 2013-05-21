(ns jiksnu.sections.conversation-sections
  (:use [ciste.sections :only [defsection]]
        [ciste.sections.default :only [delete-button full-uri uri title index-line
                                       index-block index-line index-section link-to
                                       show-section update-button]]
        [jiksnu.ko :only [*dynamic*]]
        [jiksnu.sections :only [action-link actions-section admin-index-block admin-index-line
                                bind-to control-line dropdown-menu dump-data pagination-links
                                with-page]])
  (:require [ciste.model :as cm]
            [clojure.tools.logging :as log]
            [hiccup.core :as h]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.conversation :as model.conversation]
            [jiksnu.model.domain :as model.domain]
            [jiksnu.model.feed-source :as model.feed-source]
            [jiksnu.namespace :as ns]
            [jiksnu.rdf :as rdf]
            [jiksnu.sections.user-sections :as sections.user]
            [jiksnu.session :as session]
            [plaza.rdf.core :as plaza])
  (:import jiksnu.model.Activity
           jiksnu.model.Conversation
           jiksnu.model.Domain
           jiksnu.model.FeedSource
           jiksnu.model.User))

(defn discover-button
  [item]
  (action-link "conversation" "discover" (:_id item)))

(defn model-button
  [item]
  [:a (if *dynamic*
        {:data-bind "attr: {href: '/model/conversations/' + ko.utils.unwrapObservable(_id) + '.model'}"}
        {:href (format "/model/conversations/%s.model" (:_id item))})
   "Model"])

(defn subscribe-button
  [item]
  (action-link "conversation" "subscribe" (:_id item)))

(defn unsubscribe-button
  [item]
  (action-link "conversation" "unsubscribe" (:_id item)))

(defn get-buttons
  []
  (concat
   (when (session/current-user)
     [#'subscribe-button
      #'discover-button
      #'model-button
      #'update-button])
   (when (session/is-admin?)
     [#'delete-button])))

;; actions-section

(defsection actions-section [Conversation :html]
  [item]
  (dropdown-menu item (get-buttons)))

;; admin-index-section

;; (defsection admin-index-section [Conversation :html]
;;   [page]
;;   (index-section (:items page) page))

;; admin-index-block

(defsection admin-index-block [Conversation :html]
  [records & [options & _]]
  [:table.table
   [:thead
    [:tr
     [:th "Title"]]]
   [:tbody
    (map #(admin-index-line % options) records)]])

;; delete-button

(defsection delete-button [Conversation :html]
  [user & _]
  (action-link "conversation" "delete" (:_id user)))

;; link-to

(defsection link-to [Conversation :html]
  [record & options]
  (let [options-map (apply hash-map options)]
    [:a (if *dynamic*
          {:data-bind "attr: {href: '/main/conversations/' + ko.utils.unwrapObservable(_id)}"}
          {:href (uri record)})
     [:span (merge {:property "dc:title"}
                   (if *dynamic*
                     {:data-bind "attr: {about: uri}, text: _id"}
                     {:about (uri record)}))
      (when-not *dynamic*
        (or (:title options-map) (title record)))]]))

;; index-block

;; index-block

(defsection index-block [Conversation :html]
  [items & [page]]
  [:div (when *dynamic* {:data-bind "foreach: $data"})
   (map index-line items)])

;; (defsection index-block [Conversation :html]
;;   [items & [page]]
;;   [:table.table
;;    [:thead
;;     [:tr
;;      [:th "Id"]
;;      [:th "Domain"]
;;      [:th "Url"]
;;      #_[:th "Created"]
;;      [:th "Last Updated"]
;;      [:th "Record Updated"]
;;      [:th "Actions"]]]
;;    [:tbody {:data-bind "foreach: $data"}
;;     (doall (map #(index-line % page) items))]])

(defsection index-block [Conversation :rdf]
  [items & [response & _]]
  (apply concat (map #(index-line % response) items)))

;; index-line

;; (defsection index-line [Conversation :html]
;;   [item & [page]]
;;   [:tr {:data-model "conversation"}
;;    [:td (link-to item)]
;;    [:td
;;     (let [domain (if *dynamic* (Domain.) (model.domain/fetch-by-id (:domain item)))]
;;       (bind-to "domain"
;;         [:div {:data-model "domain"}
;;          (link-to domain)]))]
;;    [:td
;;     [:a (if *dynamic*
;;           {:data-bind "attr: {href: url}, text: url"})
;;      (when-not *dynamic*
;;        (:url item))]]
;;    #_[:td (if *dynamic*
;;           {:data-bind "text: created"}
;;           (:created item))]
;;    [:td (if *dynamic*
;;           {:data-bind "text: lastUpdated"}
;;           (:lastUpdated item))]
;;    [:td (if *dynamic*
;;           {:data-bind "text: updated"}
;;           (:updated item))]
;;    [:td (actions-section item)]])

;; (defsection index-line [Conversation :html]
;;   [item & [page]]
;;   (show-section item page))

;; index-section

(defsection index-section [Conversation :atom]
  [items & [page]]
  (let [ids (map :_id items)
        page (actions.activity/fetch-by-conversations ids)]
    (index-block (:items page) page)))

;; (defsection index-section [Conversation :html]
;;   [items & [page]]
;;   (index-block items page))

;; show-section

(defn show-details
  [item & [page]]
  (list
   (actions-section item)
   [:table.table
    [:tbody
     [:tr
      [:th "Domain"]
      [:td
       (let [domain (if *dynamic* (Domain.) (model.feed-source/fetch-by-id (:domain item)))]
         (bind-to "domain"
           [:div {:data-model "domain"}
            (link-to domain)]))]]
     [:tr
      [:th "Url"]
      [:td
       [:a (if *dynamic*
             {:data-bind "attr: {href: url}, text: url"})]]]
     [:tr
      [:th "Created"]
      [:td (if *dynamic*
             {:data-bind "text: created"}
             (:created item))]]
     [:tr
      [:th "Updated"]
      [:td (if *dynamic*
             {:data-bind "text: updated"}
             (:updated item))]]
     [:tr
      [:th "Source"]
      [:td
       (let [source (if *dynamic* (FeedSource.) (model.feed-source/fetch-by-id (:update-source item)))]
         (bind-to "$data['update-source']"
           [:div {:data-model "feed-source"} (link-to source)]))]]]])
  )

;; (defn comments-section
;;   [activity]
;;   (bind-to "comments"
;;     (if-let [comments (if *dynamic*
;;                         [(Activity.)]
;;                         (seq (second (actions.comment/fetch-comments activity))))]
;;       [:section.comments
;;        [:ul.unstyled.comments
;;         (when *dynamic*
;;           {:data-bind "foreach: $data"})
;;         (map (fn [comment]
;;                [:li
;;                 (show-comment comment)])
;;              comments)]])))

(defn show-comment
  [activity]
  (let [author (if *dynamic*
                 (User.)
                 (model.activity/get-author activity))]
    [:div.comment {:data-model "activity"}
     [:p
      [:span (when *dynamic*
               {:data-bind "with: author"})
       [:span {:data-model "user"}
        (sections.user/display-avatar author)
        (link-to author)]]
      ": "
      [:span
       (if *dynamic*
         {:data-bind "text: title"}
         (h/h (:title activity)))]]
     #_[:p (posted-link-section activity)]]))

(defsection show-section [Conversation :html]
  [item & [page]]
  (let [about-uri (full-uri item)]
    [:div.conversation-section
     (merge {:data-model "conversation"}
            (when-not *dynamic*
              {:about about-uri
               :data-id (:_id item)}))
     ;; (show-details item page)
     ;; (dump-data)
     (with-page "conversation-' + $data._id() + '"
       (let [items (if *dynamic*
                     [(Activity.) (Activity.)]
                     ;; TODO: actually fetch the activity here
                     (:items (actions.activity/fetch-by-conversation item)))]
         (bind-to "activities"
           (if-let [item (first items)]
             (bind-to "$data.items[0]"
               (show-section item))
             [:p "The parent activity for this conversation could not be found"])
           (when-let [comments (next items)]
             [:section.comments (when *dynamic* {:data-bind "with: $data.items.slice(1)"})
              [:ul.unstyled.comments (when *dynamic* {:data-bind "foreach: $data"})
               (map show-comment items)]]))))]))

(defsection show-section [Conversation :rdf]
  [item & [page]]
  (plaza/with-rdf-ns ""
    (let [uri (full-uri item)]
      (rdf/with-subject uri
        [
         [[ns/rdf :type] [ns/sioc "Conversation"]]
         [[ns/dc :updated] (plaza/date (.toDate (:updated item)))]
         ]))))

;; update-button

(defsection update-button [Conversation :html]
  [item & _]
  (action-link "conversation" "update" (:_id item)))

