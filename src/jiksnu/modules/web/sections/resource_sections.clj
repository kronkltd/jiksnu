(ns jiksnu.modules.web.sections.resource-sections
  (:require [ciste.sections :refer [defsection]]
            [ciste.sections.default :refer [link-to]]
            [taoensso.timbre :as timbre])
  (:import jiksnu.model.Resource))

(defsection link-to [Resource :html]
  [source & _]
  [:a {:href "/resources/{{resource.id}}"}
   "{{resource.topic}}"])
