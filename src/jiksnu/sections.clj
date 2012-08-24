(ns jiksnu.sections
  (:use [ciste.config :only [config]]
        [ciste.sections :only [declare-section defsection]]
        [ciste.sections.default :only [full-uri title link-to index-block index-section delete-button edit-button uri index-line show-section index-block-type]]
        [jiksnu.ko :only [*dynamic*]])
  (:require [clojure.string :as string]
            [clojure.tools.logging :as log]))

(def action-icons
  {"update" "refresh"
   "delete" "trash"
   "edit" "edit"})

(def action-titles
  {"update" "Update"
   "delete" "Delete"
   "edit" "Edit"})

(defn action-link
  ([model action id]
     (action-link model action (action-titles action) (action-icons action) id))
  ([model action title icon id]
     [:a (merge {:title title
                 :class (string/join " " [(str action "-button")])
                 :data-model model
                 :data-action action}
                (if *dynamic*
                  {:href "#"}
                  {:href (str "/main/confirm"
                              "?action=" action
                              "&model=" model
                              "&id=" id)}))
      [:i {:class (str "icon-" icon)}] [:span.button-text title]]))


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
  [:li.next
   [:a {:href (str "?page=" (inc page)) :rel "next"} "Next &rarr;"]])

(defn prev-link
  [page]
  [:li.previous
   [:a {:href (str "?page=" (dec page)) :rel "prev"} "&larr; Previous"]])

(defn pagination-links
  [options]
  ;; TODO: page should always be there from now on
  (let [page (get options :page 1)
        page-size (get options :page-size 20)
        ;; If no total, no pagination
        total-records (get options :total-records 0)]
    [:div.paginations
     (when *dynamic*
       {:data-bind "with: pageInfo"})
     [:p.paginations-page
      [:span.pagination-label "Page"] " "
      [:span.pagination-value
       (if *dynamic*
         {:data-bind "text: page"}
         page)]]
     [:p.paginations-page-size
      [:span.pagination-label "Page Size"] " "
      [:span.pagination-value
       (if *dynamic*
         {:data-bind "text: pageSize"}
         page-size)]]
     [:p.paginations-record-count
      [:span.pagination-label "Records returned"] " "
      [:span.pagination-value
       (if *dynamic*
         {:data-bind "text: recordCount"}
         (count (:items options)))]]
     [:p.paginations-total-records
      [:span.pagination-label "Total Records"] " "
      [:span.pagination-value
       (if *dynamic*
         {:data-bind "text: totalRecords"}
         total-records)]]
     [:ul.pager
      (when (> page 1)
        (prev-link page))
      (when (< (* page page-size) total-records)
        (next-link page))]]))

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

(defmethod index-block-type :default
  [items & [page]]
  (->> items
       (map (fn [m] {(:_id m) (index-line m page)}))
       (into {})))

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
