(ns jiksnu.triggers.feed-source-triggers
  (:use [ciste.triggers :only [add-trigger!]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.actions.feed-source-actions :as actions.feed-source]))


(defn create-trigger
  [action params source]
  (actions.feed-source/update source))

#_(add-trigger! #'actions.feed-source/create #'create-trigger)
