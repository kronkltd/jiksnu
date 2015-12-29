(ns jiksnu.transforms.stream-transforms
  (:require [jiksnu.session :as session]
            [slingshot.slingshot :refer [throw+]]))

(defn set-owner
  [stream]
  (if-let [owner (or (:owner stream) (session/current-user-id))]
    (assoc stream :owner owner)
    (throw+ {:message "Could not determine owner"})))
