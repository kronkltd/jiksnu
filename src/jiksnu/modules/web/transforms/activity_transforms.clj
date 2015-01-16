(ns jiksnu.modules.web.transforms.activity-transforms
  (:require [ciste.config :refer [config]]
            [clj-time.core :as time]
            [clojure.tools.logging :as log]
            [clojure.string :as string]
            [jiksnu.actions.group-actions :as actions.group]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.feed-source :as model.feed-source]
            [jiksnu.ops :as ops]
            [jiksnu.session :as session]
            [jiksnu.util :as util]
            [lamina.trace :as trace]
            [slingshot.slingshot :refer [throw+]])
  (:import java.net.URI))

(defn set-url
  [activity]
  (if (seq (:url activity))
    activity
    (if (:local activity)
      (assoc activity :url "")
      (if (:id activity)
        (assoc activity :url (:id activity))
        (throw+ "Could not determine activity url")))))

