(ns jiksnu.sections
  (:use [ciste.config :only [config]]
        [ciste.sections :only [declare-section defsection]]
        [ciste.sections.default :only [full-uri title link-to index-block index-section delete-button edit-button uri index-line]])
  (:require [clojure.tools.logging :as log]))

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
     {:data-bind "with: pageInfo"}
     [:p
      [:span.pagination-label "Page"] " "
      [:span.pagination-value
       {:data-bind "text: page"}
       (when (config :html-only) page)]]
     [:p
      [:span.pagination-label "Page Size"] " "
      [:span.pagination-value
       {:data-bind "text: pageSize"}
       (when (config :html-only) page-size)]]
     [:p
      [:span.pagination-label "Records returned"] " "
      [:span.pagination-value
       {:data-bind "text: recordCount"}
       (when (config :html-only) (count (:items options)))]]
     [:p
      [:span.pagination-label "Total Records"] " "
      [:span.pagination-value
       {:data-bind "text: totalRecords"}
       (when (config :html-only) total-records)]]
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
  (index-line record options))

(defsection admin-index-section :default
  [records & [options & _]]
  (list
   (pagination-links options)
   (admin-index-block records options)))

(defsection full-uri :default
  [record & options]
  (str "http://" (config :domain)
       (apply uri record options)))

(defsection title :default
  [record & _]
  (str (:_id record)))

(defsection link-to :default
  [record & options]
  (let [options-map (apply hash-map options)]
    [:a {:href (uri record)}
     [:span {:about (uri record)
             :property "dc:title"}
      (or (:title options-map) (title record))] ]))

(defsection index-block :default
  [records & [options & _]]
  [:ul
   (map #(index-line % options) records)])


;; TODO: look for a {:paginate false} key
(defsection index-section :default
  [records & [options & _]]
  (let [{:keys [page total-records]} options]
    (list
     (pagination-links options)
     (index-block records options))))

(defsection delete-button :default
  [record & _]
  [:form {:method "post"
          :action (str (uri record) "/delete")}
   [:button.btn.delete-button {:type "submit"}
    [:i.icon-trash] [:span.button-text "Delete"]]])

(defsection edit-button :default
  [domain & _]
  [:form {:method "post" :action (str (uri domain) "/edit")}
   [:button.btn.edit-button {:type "submit"}
    [:i.icon-pencil] [:span.button-text "Edit"]]])

