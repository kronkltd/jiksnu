(ns jiksnu.sections.key-sections
  (:use [ciste.sections :only [defsection]]
        [ciste.sections.default :only [full-uri show-section index-line]]
        [jiksnu.modules.web.sections :only [action-link admin-index-line admin-index-block
                                format-links admin-index-section bind-property
                                dump-data control-line pagination-links]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.model.key :as model.key])
  (:import jiksnu.model.Key))

(defsection admin-index-block [Key :viewmodel]
  [items & [page]]
  (->> items
       (map (fn [m] (index-line m page)))
       doall))

(defsection show-section [Key :html]
  [key & _]
  ;; TODO: Rdfa
  [:div
   [:p (:n key)]
   [:p (:e key)]])

(defsection show-section [Key :viewmodel]
  [activity & [page]]
  (select-keys activity [:_id :n :e :userid]))

