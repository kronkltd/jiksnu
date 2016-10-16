(ns jiksnu.modules.json.formats
  (:require [ciste.formats :refer [format-as]]
            [clojure.data.json :as json]))

(defmethod format-as :json
  [format request response]
  (-> response
      (assoc-in [:headers "Content-Type"] "application/json")
      (assoc :body (json/write-str (:body response)))))
