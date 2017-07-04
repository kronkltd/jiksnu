(ns jiksnu.app.components.index-components
  (:require [jiksnu.app :refer [jiksnu]]
            [jiksnu.app.helpers :as helpers]
            [jiksnu.registry :as registry]))

(doseq [[page-name] registry/page-mappings]
  (helpers/page-controller jiksnu page-name))
