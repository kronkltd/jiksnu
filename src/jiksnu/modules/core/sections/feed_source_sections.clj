(ns jiksnu.modules.core.sections.feed-source-sections
  (:require [ciste.sections :refer [defsection]]
            [ciste.sections.default :refer [actions-section delete-button
                                            show-section index-line index-block
                                            index-section link-to title
                                            update-button]]
            [clojure.string :as string]
            [clojure.tools.logging :as log]
            [jiksnu.model.user :as model.user]
            [jiksnu.modules.core.sections :refer [admin-show-section
                                                  admin-index-block admin-index-line
                                                  admin-index-section]])
  (:import jiksnu.model.FeedSource
           jiksnu.model.User))

(defsection admin-index-block [FeedSource]
  [items & [page]]
  (map #(admin-index-line % page) items))

(defsection admin-index-block [FeedSource :viewmodel]
  [items & [page]]
  (->> items
       (map (fn [m] (index-line m page)))
       doall))

(defsection admin-index-section [FeedSource]
  [items & [page]]
  (admin-index-block items page))

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

(defsection title [FeedSource]
  [item & _]
  (:title item))

