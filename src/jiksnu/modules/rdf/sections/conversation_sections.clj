(ns jiksnu.modules.rdf.sections.conversation-sections
  (:use [ciste.sections :only [defsection]]
        [ciste.sections.default :only [full-uri uri index-line
                                       index-block show-section]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.namespace :as ns]
            [jiksnu.modules.rdf.util :as rdf]
            [plaza.rdf.core :as plaza])
  (:import jiksnu.model.Conversation))

(defsection index-block [Conversation :rdf]
  [items & [response & _]]
  (apply concat (map #(index-line % response) items)))

(defsection show-section [Conversation :rdf]
  [item & [page]]
  (plaza/with-rdf-ns ""
    (let [uri (full-uri item)]
      (rdf/with-subject uri
        [
         [[ns/rdf :type] [ns/sioc "Conversation"]]
         [[ns/dc :updated] (plaza/date (.toDate (:updated item)))]
         ]))))
