(ns jiksnu.triggers.feed-source-triggers
  (:use [ciste.triggers :only [add-trigger!]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.actions.feed-source-actions :as actions.feed-source]))


(defn create-trigger
  "This will cause every new source to be updated.

Note. This causes a lot of records to be created"
  [action params source]
  (actions.feed-source/update source))

#_(add-trigger! #'actions.feed-source/create #'create-trigger)
