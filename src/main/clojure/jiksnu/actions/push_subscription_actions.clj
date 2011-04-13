(ns jiksnu.actions.push-subscription-actions
  (:use ciste.core
        ciste.debug
        jiksnu.model
        jiksnu.namespace
        jiksnu.session
        [karras.entity :only (make)])
  (:require (jiksnu.model
             [push-subscription :as model.push])
            jiksnu.view)
  (:import jiksnu.model.Activity
           org.apache.abdera.model.Entry))

(defaction index
  [options]
  (model.push/index))
