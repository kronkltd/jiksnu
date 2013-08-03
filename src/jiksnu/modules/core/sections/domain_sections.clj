(ns jiksnu.modules.core.sections.domain-sections
  (:use [ciste.core :only [with-format]]
        [ciste.sections :only [defsection]]
        [ciste.sections.default :only [actions-section add-form delete-button index-block index-line
                                       link-to show-section uri]]
        [clojurewerkz.route-one.core :only [named-path]]
        [jiksnu.ko :only [*dynamic*]]
        [jiksnu.session :only [current-user is-admin?]]
        [jiksnu.modules.core.sections :only [admin-index-block admin-index-line]]
        [jiksnu.modules.web.sections :only [action-link bind-to control-line display-property
                                            dropdown-menu]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.namespace :as ns]
            [jiksnu.session :as session])
  (:import jiksnu.model.Domain))

;; admin-index-block

(defsection admin-index-block [Domain :viewmodel]
  [items & [page]]
  (->> items
       (map (fn [m] {(:_id m) (admin-index-line m page)}))
       (into {})))

(defsection index-block [Domain :viewmodel]
  [items & [page]]
  (->> items
       (map (fn [m] (index-line m page)))
       doall))

(defsection show-section [Domain :jrd]
  [item & [page]]
  (let [id (:_id item)
        template (format "http://%s/main/xrd?uri={uri}" id)]
    {:host id
     :links [{:template template
              :rel "lrdd"
              :title "Resource Descriptor"}]}))

(defsection show-section [Domain :json]
  [item & [page]]
  (with-format :jrd (show-section item page)))

(defsection show-section [Domain :model]
  [item & [page]]
  item)

(defsection show-section [Domain :viewmodel]
  [item & [page]]
  item)

(defsection show-section [Domain :xrd]
  [item & [page]]
  (let [id (:_id item)]
    ["XRD" {"xmlns" ns/xrd
            "xmlns:hm" ns/host-meta}
     ["hm:Host" id]
     ["Subject" id]
     (->> (:links item)
          (map
           ;; TODO: show-section [Link :xrd]
           (fn [{:keys [title rel href template] :as link}]
             [:Link (merge {}
                           (if rel {:rel rel})
                           (if href {:href href})
                           (if template {:template template}))
              (if title
                [:Title title])])))]))

;; uri

(defsection uri [Domain]
  [domain & _]
  (str "/main/domains/" (:_id domain)))
