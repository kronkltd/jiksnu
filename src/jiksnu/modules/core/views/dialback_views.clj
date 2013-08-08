(ns jiksnu.modules.core.views.dialback-views
  (:use [ciste.config :only [config]]
        [ciste.views :only [defview]]
        ciste.sections.default
        [clojurewerkz.route-one.core :only [named-path]]
        jiksnu.actions.dialback-actions
        [jiksnu.ko :only [*dynamic*]]
        [jiksnu.modules.web.sections :only [bind-to]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.model :as model]
            [ring.util.response :as response]))

(defview #'confirm :html
  [request activity]
  {:body ""
   :template false})
