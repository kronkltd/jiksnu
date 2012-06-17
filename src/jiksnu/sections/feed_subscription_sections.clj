(ns jiksnu.sections.feed-subscription-sections
  (:use [ciste.model :only [implement]]
        [ciste.sections :only [defsection]]
        [ciste.sections.default :only [add-form delete-button show-section index-line
                                       index-section link-to update-button]]
        [jiksnu.sections :only [control-line]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.model.user :as model.user])
  (:import jiksnu.model.FeedSubscription))

(defsection add-form [FeedSubscription :html]
  [subscription & [options & _]]
  (implement
      [:form]
      )

  )
