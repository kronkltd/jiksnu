(ns jiksnu.modules.core.sections.auth-sections
  (:use [ciste.sections :only [defsection]]
        [ciste.sections.default :only [index-block index-line show-section]]
        [jiksnu.modules.core.sections :only [admin-index-block admin-index-line]])
  (:require [clojure.tools.logging :as log])
  (:import jiksnu.model.AuthenticationMechanism))

(defsection admin-index-block [AuthenticationMechanism :viewmodel]
  [items & [page]]
  (->> items
       (map (fn [m] (admin-index-line m page)))
       doall))

;; index-block

(defsection index-block [AuthenticationMechanism :viewmodel]
  [items & [page]]
  (->> items
       (map (fn [m] (index-line m page)))
       doall))

;; show-section

(defsection show-section [AuthenticationMechanism :model]
  [item & [page]]
  item)

(defsection show-section [AuthenticationMechanism :viewmodel]
  [item & [page]]
  item)

