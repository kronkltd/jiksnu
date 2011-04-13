(ns jiksnu.model.push-subscription
  (:use ciste.debug
        jiksnu.model
        [jiksnu.session :only (current-user current-user-id is-admin?)])
  (:require [clojure.string :as string]
            [karras.entity :as entity]
            [karras.sugar :as sugar])
  (:import jiksnu.model.PushSubscription))

(defn index
  []
  (entity/fetch PushSubscription {}))
