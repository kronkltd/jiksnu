(ns jiksnu.modules.core.sections.key-sections
  (:use [ciste.sections :only [defsection]]
        [ciste.sections.default :only [show-section index-line]]
        [jiksnu.modules.core.sections :only [admin-index-block]])
  (:require [clojure.tools.logging :as log])
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

