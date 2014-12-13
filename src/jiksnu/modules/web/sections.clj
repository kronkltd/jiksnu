(ns jiksnu.modules.web.sections
  (:use [ciste.config :only [config]]
        [ciste.sections :only [declare-section defsection]]
        [ciste.sections.default :only [edit-button index-block
                                       index-block-type index-line uri]]
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
  [:span {:data-page page-name}
   body])

(defn with-sub-page
  [page-name & body]
  [:span {:data-sub-page page-name}
   body])

(defn bind-property
  [property]
  {:data-bind
   (str "text: typeof($data."
        property
        ") !== 'undefined' ? "
        property
        " : ''")})

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

(defn display-property
  [item property]
  (if *dynamic*
    (str "{{" (name property) "}}")
    (str (get item (keyword property)))))

(defn display-timestamp
  [item property]
  [:time.timeago
   {:data-toggle "timeago"
    :datetime (str "{{" (name property) "}}")}
   (str "{{" (name property) "}}")])

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
  [:div
   [:div.pull-left
    [:a.previous {:rel "prev"
                  :href "?page={{page.page - 1}}"}
     [:i.icon-left-arrow] " Previous"]]
   [:p.pull-left
    "Page {{page.page}}"
    ". (showing {{((page.page - 1) * page.pageSize) + 1}}"
    " to {{page.page * page.pageSize}}"
    " of {{page.totalRecords}}"
    " records)"]
   [:div.pull-right
    [:a.next {:rel "next"
              :href "?page={{page.page + 1}}"}
     "Next "
     [:i.icon-right-arrow]]]
   [:div.clearfix]])
