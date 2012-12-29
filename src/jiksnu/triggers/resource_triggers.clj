(ns jiksnu.triggers.resource-triggers
  (:use [ciste.triggers :only [add-trigger!]]
        [slingshot.slingshot :only [throw+]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.actions.resource-actions :as actions.resource]
            [jiksnu.ops :as ops]))

(defn add-link-trigger
  [action [user link] _]
  (condp = (:rel link)
    "alternate" (if (= (:type link) "application/atom+xml")
                  (let [source (ops/get-source (:href link))]
                    (model.resource/set-field! item :updateSource (:_id source))
                    #_(actions.feed-source/update source)))
    nil))

(add-trigger! #'actions.resource/add-link*     #'add-link-trigger)
