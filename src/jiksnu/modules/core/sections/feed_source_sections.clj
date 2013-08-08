(ns jiksnu.modules.core.sections.feed-source-sections
  (:use [ciste.sections :only [defsection]]
        [ciste.sections.default :only [actions-section add-form delete-button show-section
                                       index-line index-block index-section link-to title
                                       update-button]]
        [clojurewerkz.route-one.core :only [named-path]]
        [jiksnu.ko :only [*dynamic*]]
        [jiksnu.modules.core.sections :only [admin-show-section
                                             admin-index-block admin-index-line
                                             admin-index-section]]
        [jiksnu.modules.web.sections :only [action-link bind-to control-line display-property
                                            dropdown-menu dump-data]])
  (:require [clojure.string :as string]
            [clojure.tools.logging :as log]
            [jiksnu.model.user :as model.user]
            [jiksnu.session :as session])
  (:import jiksnu.model.FeedSource
           jiksnu.model.User))

;; admin-index-block

(defsection admin-index-block [FeedSource]
  [items & [page]]
  (map #(admin-index-line % page) items))

(defsection admin-index-block [FeedSource :viewmodel]
  [items & [page]]
  (->> items
       (map (fn [m] (index-line m page)))
       doall))

;; admin-index-section

(defsection admin-index-section [FeedSource]
  [items & [page]]
  (admin-index-block items page))

;; admin-show-section

(defsection admin-show-section [FeedSource]
  [item & [page]]
  (show-section item))

(defsection index-block [FeedSource :viewmodel]
  [items & [page]]
  (->> items
       (map (fn [m] (index-line m page)))
       doall))

(defsection show-section [FeedSource :model]
  [activity & [page]]
  activity)

(defsection show-section [FeedSource :viewmodel]
  [item & _]
  item)

;; title

(defsection title [FeedSource]
  [item & _]
  (:title item))

