(ns jiksnu.modules.core
  (:require [ciste.initializer :refer [definitializer]]
            jiksnu.modules.core.formats
            [jiksnu.registry :as registry]
            [jiksnu.util :as util]))

(def module
  {:name "jiksnu-core"
   :deps []
   }
  )

(definitializer
  (doseq [model-name registry/action-group-names]
    (doseq [module-name registry/module-names]
      (util/require-module "jiksnu.modules" module-name model-name))))
