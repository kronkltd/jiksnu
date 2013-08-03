(ns jiksnu.modules.core.sections
  (:use [ciste.config :only [config]]
        [ciste.sections :only [declare-section defsection]]
        [ciste.sections.default :only [delete-button edit-button full-uri index-block
                                       index-block-type index-line index-section
                                       link-to show-section title uri]]
        [jiksnu.ko :only [*dynamic*]])
  (:require [clojure.string :as string]
            [clojure.tools.logging :as log]
            [inflections.core :as inf]))

;; (declare-section actions-section)
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
