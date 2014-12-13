(ns jiksnu.modules.web.sections.feed-source-sections
  (:require [ciste.sections :refer [defsection]]
            [ciste.sections.default :refer [actions-section add-form delete-button show-section
                                            index-line index-block index-section link-to title
                                            update-button]]
            [clojure.string :as string]
            [clojure.tools.logging :as log]
            [jiksnu.ko :refer [*dynamic*]]
            [jiksnu.model.user :as model.user]
            [jiksnu.modules.core.sections :refer [admin-show-section
                                                  admin-index-block admin-index-line
                                                  admin-index-section]]
            [jiksnu.modules.web.sections :refer [action-link bind-to control-line display-property
                                                 dropdown-menu]]
            [jiksnu.session :as session])
  (:import jiksnu.model.FeedSource
           jiksnu.model.User))

(defn subscribe-button
  [item & _]
  (action-link "feed-source" "subscribe" (:_id item)))

(defn unsubscribe-button
  [item & _]
  (action-link "feed-source" "unsubscribe" (:_id item)))

(defn index-watchers
  [source]
  [:div.watchers
   [:h3 "Watchers "
    [:span (if *dynamic*
             {:data-bind "text: watchers.length"}
             (count (:watchers source)))]]
   (bind-to "watchers"
     [:table.table
      [:tbody
       (when *dynamic* {:data-bind "foreach: items"})
       (let [watchers (if *dynamic* [""] (:watchers source))]
         (map
          (fn [id]
            (let [user (if *dynamic* (User.) (model.user/fetch-by-id id))]
              [:tr (merge
                    {:data-model "user"}
                    #_(if *dynamic*
                        {:data-bind
                         (string/join ", "
                                      ["if: $data"
                                       "attr: {'data-target': $data}"])}))
               [:td (link-to user)]
               [:td
                (action-link "feed-source" "remove-watcher" (:_id source)
                             {:target (:_id user)
                              :icon "trash"
                              :title "Delete"})]]))
          watchers))]])])

(defn add-watcher-form
  [source]
  [:form.well.form-horizontal
   {:method "post"
    :action (format "/admin/feed-sources/%s/watchers"
                    (:_id source))}
   [:fieldset
    [:legend "Add Watcher"]
    (control-line "Acct id"
                  :user_id "text")
    [:input {:type "submit"}]]])

(defn watch-button
  [item]
  (action-link "feed-source" "watch" (:_id item)))

(defn unwatch-button
  [item]
  (action-link "feed-source" "unwatch" (:_id item)))

(defn model-button
  [item]
  [:a (if *dynamic*
        {:data-bind "attr: {href: '/model/feed-sources/' + _id() + '.model'}"}
        {:href (format "/model/feed-sources/%s.model" (:_id item))})
   "Model"])

(defn get-buttons
  []
  (concat
   [#'model-button]
   (when (session/current-user)
     [#'update-button
      #'subscribe-button
      #'unsubscribe-button
      #'watch-button
      #'unwatch-button])
   (when (session/is-admin?)
     [#'delete-button])))

;; actions-section

(defsection actions-section [FeedSource :html]
  [item]
  (dropdown-menu item (get-buttons)))

;; add-form

(defsection add-form [FeedSource :html]
  [source & options]
  [:form.well.form-horizontal {:method "post" :action "/admin/feed-sources"}
   [:fieldset
    [:legend "Add Source"]
    (control-line "Topic"  "topic" "text")
    (control-line "Callback" "callback" "text")
    (control-line "Challenge" "challenge" "text")
    ;; TODO: radio buttons?
    (control-line "Mode" "mode" "text")
    (control-line "User" "user" "text")

    [:div.form-actions
     [:button.btn.btn-primary
      {:type "submit"} "Add"]]]])

(defsection admin-index-block [FeedSource :html]
  [items & [options & _]]
  [:table.table
   [:thead
    [:tr
     [:th "Id"]
     [:th "Title"]
     [:th "Domain"]
     [:th "Topic"]
     [:th "Status"]
     [:th "Actions"]]]
   [:tbody
    (when *dynamic* {:data-bind "foreach: items"})
    (map admin-index-line items)]])

;; admin-index-line

(defsection admin-index-line [FeedSource :html]
  [item & [page]]
  [:tr (merge {:data-model "feed-source"}
              (when-not *dynamic*
                {:data-id (:_id item)}))
   [:td
    (link-to item)]
   [:td
    [:a (if *dynamic*
          {:data-bind "attr: {href: '/admin/feed-sources/' + _id()}, text: title"}
          {:title (:title item)
           :href (str "/admin/feed-sources/" (:_id item))})
     (when-not *dynamic*
       (:title item))]]
   [:td (display-property item :domain)]
   [:td
    [:a (if *dynamic*
          {:data-bind "attr: {href: topic}, text: topic"}
          {:href (:topic item)})
     (when-not *dynamic* (:topic item))]]
   [:td (display-property item :status)]
   [:td (actions-section item)]])

(defsection admin-show-section [FeedSource :html]
  [item & [page]]
  (show-section item))

;; delete-button

(defsection delete-button [FeedSource :html]
  [item & _]
  (action-link "feed-source" "delete" (:_id item)))

(defsection index-block [FeedSource :html]
  [sources & _]
  [:table.table.feed-sources
   [:thead
    [:tr
     [:th "Title"]
     [:th "Domain"]
     [:th "Topic"]
     [:th "Hub"]
     #_[:th "Mode"]
     [:th "Status"]
     [:th "Watchers"]
     [:th "Updated"]
     [:th "Actions"]]]
   [:tbody
    (when *dynamic* {:data-bind "foreach: items"})
    (map index-line sources)]])

;; index-line

(defsection index-line [FeedSource :html]
  [source & _]
  [:tr {:data-model "feed-source"}
   [:td (link-to source)]
   [:td (display-property source :domain)]
   [:td
    [:a (if *dynamic*
          {:data-bind "attr: {href: topic}, text: topic"}
          {:href (:topic source)})
     (when-not *dynamic*
       (:topic source))]]
   [:td (display-property source :hub)]
   #_[:td (:mode source)]
   [:td (if *dynamic*
          {:data-bind "text: status"}
          (str (:status source)))]
   [:td (if *dynamic*
          {:data-bind "text: ko.utils.unwrapObservable(watchers).length"}
          (count (:watchers source)))]
   [:td (display-property source :updated)]
   [:td (actions-section source)]])

;; index-section

(defsection index-section [FeedSource :html]
  [sources & [options & _]]
  (index-block sources options))

;; link-to

(defsection link-to [FeedSource :html]
  [source & _]
  [:a (if *dynamic*
        {:data-bind "attr: {href: '/main/feed-sources/' + _id()}, text: title"}
        {:href (str "/admin/feed-sources/" (:_id source))})
   (:topic source)])

;; show-section

(defsection show-section [FeedSource :html]
  [source & options]
  (let [{:keys [verify-token lease-seconds ]} source]
    [:div {:data-model "feed-source"}
     (actions-section source)
     [:table.table
      [:tbody
       [:tr
        [:th "Topic:"]
        [:td
         [:a {:href "{{source.topic}}"}
          "{{source.topic}}"]]]
       [:tr
        [:th "Domain:"]
        [:td (display-property source :domain)]]
       [:tr
        [:th "Hub:"]
        [:td [:a {:href "{{hub}}"}
              "{{hub}}"]]]
       [:tr
        [:th "Callback:"]
        [:td (display-property source :callback)]]
       [:tr
        [:th  "Challenge:"]
        [:td (display-property source :challenge)]]
       [:tr
        [:th "Mode:"]
        [:td (display-property source :mode)]]
       [:tr
        [:th "Status:"]
        [:td (display-property source :status)]]
       [:tr
        [:th "Verify Token:"]
        [:td verify-token]]
       [:tr
        [:th "Created:"]
        [:td (display-property source :created)]]
       [:tr
        [:th "Updated:"]
        [:td (display-property source :updated)]]
       [:tr
        [:th "Lease Seconds:"]
        [:td lease-seconds]]]]]))

;; update-button

(defsection update-button [FeedSource :html]
  [item & _]
  (action-link "feed-source" "update" (:_id item)))
