(ns jiksnu.modules.xmpp.filters.stream-filters
  (:use [ciste.filters :only [deffilter]]
        [clojure.core.incubator :only [-?>]]
        jiksnu.actions.stream-actions)
  (:require [clojure.tools.logging :as log]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.model :as model]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.group :as model.group]
            [jiksnu.model.user :as model.user]
            [jiksnu.util :as util]))

(deffilter #'public-timeline :xmpp
  [action request]
  (action))

(deffilter #'user-timeline :xmpp
  [action request]
  (-> request :to
      model.user/fetch-by-jid action))
