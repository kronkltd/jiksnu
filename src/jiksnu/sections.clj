(ns jiksnu.sections
  (:use [ciste.config :only [config]]
        [ciste.sections :only [declare-section defsection]]
        [ciste.sections.default :only [delete-button edit-button full-uri index-block
                                       index-block-type index-line index-section
                                       link-to show-section title uri]]
        [jiksnu.ko :only [*dynamic*]])
  (:require [clojure.string :as string]
            [clojure.tools.logging :as log]
            [inflections.core :as inf]))

(def action-icons
  {"update"      "refresh"
   "delete"      "trash"
   "edit"        "edit"
   "discover"    "search"
   "subscribe"   "eye-open"
   "watch"       "eye-open"
   "unsubscribe" "eye-close"})

(def action-titles
  {"update"      "Update"
   "delete"      "Delete"
   "edit"        "Edit"
   "discover"    "Discover"
   "watch"       "Watch"
   "subscribe"   "Subscribe"
   "unsubscribe" "Unsubscribe"})

(def format-links
  {
   :as        {:label "Activity Streams"
               :icon  "as-bw-14x14.png"
               :type  "application/json"}
   :atom      {:label "Atom"
               :icon  "feed-icon-14x14.png"
               :type  "application/atom+xml"}
   :json      {:label "JSON"
               :icon  "json.png"
               :type  "application/json"}
   :n3        {:label "N3"
               :type  "text/n3"
               :icon  "chart_organisation.png"}
   :rdf       {:label "RDF/XML"
               :icon  "foafTiny.gif"
               :type  "application/rdf+xml"}
   :xml       {:label "XML"
               :icon  "file_xml.png"
               :type  "application/xml"}
   :viewmodel {:label "Viewmodel"
               :icon ""
               :type  "application/json"}})

(defn format-page-info
  [page]
  (into {}
        (map
         (fn [[k v]]
           [(inf/camelize (name k) :lower) v])
         (assoc page :items (map :_id (:items page))))))

(defn action-link
  [model action id & [options]]
  (let [title (or (:title options) (action-titles action))
        icon (or (:icon options) (action-icons action))
        target (:target options)]
    [:a (merge
         {:title title
          :class (string/join " " [(str action "-button")])
          ;; :data-model model
          }
         (if *dynamic*
           {:href "#"
            :data-action action}
           {:href (str "/main/confirm"
                       "?action=" action
                       "&model=" model
                       "&id=" id
                       (if target
                         (str "&target=" target)))})
         (if target
           {:data-target target}))
     [:i {:class (str "icon-" icon)}]
     [:span.button-text title]]))

(defn bind-to
  [property & body]
  (if *dynamic*
    (list
     (format "<!-- ko with: %s -->" (name property))
     body
     "<!-- /ko -->")
    body))

(defn with-page
  [page-name & body]
  [:span (when *dynamic*
           {:data-page page-name})
   body])

(defn with-sub-page
  [page-name & body]
  [:span (when *dynamic* {:data-sub-page page-name})
   body])

(defn bind-property
  [property]
  {:data-bind
   (str "text: typeof($data."
        property
        ") !== 'undefined' ? "
        property
        " : ''")})

(defn dump-data
  []
  [:pre.prettyprint
   {:data-bind "text: JSON.stringify(ko.toJS($data), undefined, 2)"}])

(defn control-line
  [label name type & {:as options}]
  (let [{:keys [value checked]} options]
    [:div.control-group
     [:label.control-label {:for name} label]
     [:div.controls
      [:input
       (merge {:type type :name name}
              (when value
                {:value value})
              (when checked
                {:checked "checked"}))]]]))

(defn next-link
  [page]
  [:a.next (merge {:rel "next"}
                  (if *dynamic*
                    {:data-bind "attr: {href: '?page=' + (1 + page())}"}
                    {:href (str "?page=" (inc page))}))
   "Next "
   [:i.icon-right-arrow]])

(defn prev-link
  [page]
  [:a.previous (merge {:rel "prev"}
                      (if *dynamic*
                        {:data-bind "attr: {href: '?page=' + (0 + page() - 1)}"}
                        {:href (str "?page=" (dec page)) }))
   [:i.icon-left-arrow] " Previous"])

(defn display-property
  [item property]
  (if *dynamic*
    (list
     (format "<!-- ko text: %s -->" (name property))
     "<!-- /ko -->")
    (str (get item (keyword property)))))

(defn display-timestamp
  [item property]
  [:time.timeago
   (merge
    {:data-toggle "timeago"}
    (if *dynamic*
      {:data-bind
       (format "attr: {datetime: %s}, text: %s"
               (name property)
               (name property))}
      {:datetime (str (get item (keyword property)))}))
   (when-not *dynamic*
     (str (get item (keyword property))))])

(defn dropdown-menu
  [item buttons]
  (when (seq buttons)
    [:div.btn-group.pull-right
     [:a.btn.dropdown-toggle {:data-toggle "dropdown"}
      [:span.caret]]
     [:ul.dropdown-menu.pull-right
      (map
       (fn [button-fn]
         [:li (button-fn item)])
       buttons)]]))

(defn pagination-links
  [options]
  ;; TODO: page should always be there from now on
  (let [page (get options :page 1)
        page-size (get options :page-size 20)
        ;; If no total, no pagination
        total-records (get options :totalRecords 0)]
    [:div
     [:div.pull-left
      (prev-link page)]
     [:p.pull-left
      "Page " [:span
               (if *dynamic*
                 {:data-bind "text: page"}
                 page)]
      ". (showing " [:span
                     (if *dynamic*
                       {:data-bind "text: ((page() - 1) * pageSize()) + 1"}
                       (inc (* (dec page) page-size)))]
      " to " [:span (if *dynamic*
                      {:data-bind "text: (page() * pageSize())"}
                      (* page page-size))]
      " of " [:span (if *dynamic*
                      {:data-bind "text: totalRecords"}
                      total-records)]
      " records)"]
     [:div.pull-right
      (next-link page)]
     [:div.clearfix]]))

(declare-section actions-section)
(declare-section admin-actions-section)
(declare-section admin-index-section :seq)
(declare-section admin-index-block :seq)
(declare-section admin-index-line)
(declare-section admin-show-section)

(defsection admin-index-block :default
  [records & [options & _]]
  (map #(index-block % options) records))

(defsection admin-index-line :default
  [record & [options]]
  (admin-show-section record options))

(defsection admin-index-section :default
  [items & [page]]
  (admin-index-block items page))

(defsection admin-show-section :default
  [item & [page]]
  (show-section item page))

;; TODO: only for html
;; (defsection delete-button :default
;;   [record & _]
;;   ;; (log/debug "delete-button :default")
;;   [:form {:method "post"
;;           :action (str (uri record) "/delete")}
;;    [:button.btn.delete-button {:type "submit"}
;;     [:i.icon-trash] [:span.button-text "Delete"]]])

;; TODO: only for html
;; (defsection edit-button :default
;;   [domain & _]
;;   [:form {:method "post" :action (str (uri domain) "/edit")}
;;    [:button.btn.edit-button {:type "submit"}
;;     [:i.icon-pencil] [:span.button-text "Edit"]]])

(defsection full-uri :default
  [record & options]
  (str "http://" (config :domain)
       (apply uri record options)))

(defsection index-block :default
  [items & [page]]
  (map #(index-line % page) items))

;; (defmethod index-block-type :default
;;   [items & [page]]
;;   (->> items
;;        (map (fn [m] {(:_id m) (index-line m page)}))
;;        (into {})))

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
      (or (:title options-map) (title record))] ]))

(defsection title :default
  [record & _]
  (str (:_id record)))
