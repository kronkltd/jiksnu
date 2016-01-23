(ns jiksnu.modules.web.sections.conversation-sections
  (:require [ciste.sections :refer [defsection]]
            [ciste.sections.default :refer [actions-section title index-line index-block
                                            index-line link-to update-button]]
            [jiksnu.modules.core.sections :refer [admin-index-block admin-index-line]]
            [jiksnu.modules.web.sections :refer [action-link bind-to dropdown-menu]])
  (:import jiksnu.model.Domain
           jiksnu.model.FeedSource))

(defn show-details
  [item & [page]]
  (list
   (actions-section item)
   [:table.table
    [:tbody
     [:tr
      [:th "Domain"]
      [:td
       (if-let [domain (Domain.)]
         (bind-to "domain"
                  [:div {:data-model "domain"}
                   (link-to domain)]))]]
     [:tr
      [:th "Url"]
      [:td
       [:a {:href "{{conversation.url}}"}
        "{{conversation.url}}"]]]
     [:tr
      [:th "Item Count"]
      [:td "{{conversation.itemCount}}"]]
     [:tr
      [:th "Created"]
      [:td "{{conversation.created}}"]]
     [:tr
      [:th "Updated"]
      [:td "{{conversation.updated}}"]]
     [:tr
      [:th "Last Updated"]
      [:td "{{conversation.lastUpdated}}"]]
     [:tr
      [:th "Source"]
      [:td
       (let [source (FeedSource.)]
         (bind-to "$data['update-source']"
                  [:div {:data-model "feed-source"}
                   (link-to source)]))]]]]))
