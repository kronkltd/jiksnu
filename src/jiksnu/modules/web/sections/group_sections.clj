(ns jiksnu.modules.web.sections.group-sections
  (:require [ciste.sections :refer [defsection]]
            [ciste.sections.default :refer [link-to]])
  (:import jiksnu.model.Group))

(defsection link-to [Group :html]
  [item & options]
  [:a
   {:href "/main/groups/{{group.nickname}}"}
   [:span {:about "{{group.url}}"
           :property "dc:title"}
    "{{group.nickname}}"]])
