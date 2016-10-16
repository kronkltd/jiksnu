(ns jiksnu.modules.json.formats
  (:require [ciste.formats :refer [format-as]]
            [clojure.data.json :as json]))

(defmethod format-as :json
  [format request response]
  (json/write-str response))
