(ns jiksnu.views.setting-views
  (:use [ciste.views :only [defview]]
        [clojurewerkz.route-one.core :only [named-path]]
        jiksnu.actions.setting-actions)
  (:require [hiccup.form :as f]
            [jiksnu.sections.setting-sections :as sections.setting]))

(defview #'config-output :json
  [request data]
  {:body data})

