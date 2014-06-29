(ns jiksnu.transforms.user-transforms
  (:require [ciste.config :refer [config]]
            [ciste.model :as cm]
            [clj-gravatar.core :refer [gravatar-image]]
            [clojure.tools.logging :as log]
            [clojurewerkz.route-one.core :refer [named-url]]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.model.domain :as model.domain]
            [jiksnu.model.user :as model.user]
            [jiksnu.model.webfinger :as model.webfinger]
            [jiksnu.routes.helpers :refer [formatted-url]]
            [jiksnu.ops :as ops]
            [jiksnu.util :as util]
            [slingshot.slingshot :refer [throw+]])
  (:import java.net.URI))

(defn salmon-link
  [user]
  #_(named-url "user salmon" {:id (:_id user)}))


(defn set-url
  [user]
  (if (:url user)
    user
    (assoc user :url "" #_(named-url "local user timeline" user))))

