(ns jiksnu.modules.command.filters.stream-filters
  (:require [ciste.filters :refer [deffilter]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.stream-actions :as actions.stream]))

(deffilter #'actions.stream/public-timeline :command
  [action request]
  (action))

