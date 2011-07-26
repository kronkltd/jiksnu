(ns jiksnu.templates.layout
  (:use closure.templates.core))

(deftemplate layout
  [response]
  {:body (:body response)})
